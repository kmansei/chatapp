package chatapp;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Server {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new ServerHandler());
                        }
                    })
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

class ServerHandler extends ChannelInboundHandlerAdapter {
    // すべてのアクティブなクライアントを保持するリストまたは集合
    private static final Set<ChannelHandlerContext> channels = ConcurrentHashMap.newKeySet();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 新しいクライアントが接続した際にリストまたは集合に追加
        channels.add(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // クライアントが切断した際にリストまたは集合から削除
        channels.remove(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // クライアントから受信したメッセージを出力
        String message = (String) msg;
        System.out.println(message);

        // 他の接続している全てのクライアントにブロードキャスト
        for (ChannelHandlerContext channel : channels) {
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