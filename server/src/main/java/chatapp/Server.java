package chatapp;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.channel.ChannelHandler.Sharable;

public class Server {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    // すべてのアクティブなクライアントを保持するセット
    public static final Set<ChannelHandlerContext> channels = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        var initializer = new ServerInitializer();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(initializer)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // ホストアドレスとポート番号を指定してサーバーを起動
            ChannelFuture f = b.bind(HOST, PORT).sync();
            System.out.println("チャットサーバーを起動");

            // ソケットが閉じるまで待機
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

class ServerInitializer extends ChannelInitializer<SocketChannel>  {
    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();
    private static final ServerHandler HANDLER = new ServerHandler();

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);
        pipeline.addLast(HANDLER);
    }
}

@Sharable
class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 新しいクライアントが接続した際にセットに追加
        Server.channels.add(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // クライアントが切断した際にセットから削除
        Server.channels.remove(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // クライアントから受信したメッセージを出力
        String message = (String) msg;
        System.out.println(message);

        // 他の接続している全てのクライアントにブロードキャスト
        for (ChannelHandlerContext channel : Server.channels) {
            if (channel != ctx) {
                channel.writeAndFlush(message);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}