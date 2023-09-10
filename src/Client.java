import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Client {
    private final String HOST = "localhost";
    private final int PORT = 1234;

    private SocketChannel socket;

    public static void main(String[] args) {
        new Client();
    }

    Client() {
        start();
    }

    private void start() {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
                Scanner scanner = new Scanner(System.in)) {

            socket = socketChannel;

            // ノンブロッキングモード
            socketChannel.configureBlocking(false);
            var PID = ProcessHandle.current().pid();
            System.out.println("チャットサーバーに接続しました (PID: " + PID + ")");

            // サーバーからのメッセージを受信するスレッドを起動
            Executors.newSingleThreadExecutor().execute(new ServerListener(socketChannel));

            var buffer = ByteBuffer.allocate(1024);

            while (true) {
                // ユーザーのを入力を待つ
                String input = scanner.nextLine();

                // サーバーにメッセージを送信
                sendToServer(socketChannel, buffer, PID + ": " + input);
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバーとの接続を停止");
        }
    }

    // サーバーにメッセージを送信
    private void sendToServer(SocketChannel channel, ByteBuffer buffer, String message) throws Exception {
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    // テスト用のメソッド
    public boolean isConnectedTest() {
        return socket != null && socket.isConnected();
    }
}

class ServerListener implements Runnable {
    private SocketChannel channel;

    ServerListener(SocketChannel channel) {
        this.channel = channel;
    }

    public void run() {
        var buffer = ByteBuffer.allocate(1024);
        try (Selector selector = Selector.open()) {
            channel.register(selector, SelectionKey.OP_READ);

            // イベントが発生するまで待機
            while (selector.select() > 0) {

                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isReadable()) {
                        buffer.clear();
                        var read = channel.read(buffer);
                        if (read > 0) {
                            String response = new String(buffer.array(), 0, read);
                            System.out.println(response);
                        }
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバーとの接続を停止");
        }
    }
}