package chatapp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try (Scanner scanner = new Scanner(System.in)) {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder(), new StringEncoder(), new ClientHandler());
                        }
                    });

            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("チャットサーバーに接続");
            Channel channel = f.channel();

            // プロセスIDを取得
            var PID = ProcessHandle.current().pid();

            while (true) {
                String msg = scanner.nextLine();
                // サーバーにメッセージを送信
                channel.writeAndFlush(PID + ": " + msg);
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}

class ClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
