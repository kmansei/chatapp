import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// チャットサーバー
public class Server {
    public static final String HOST = "localhost";
    public static final int PORT = 1234;

    // スレッドセーフなハッシュセット
    public static final Set<SocketChannel> clientChannels = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        new Server();
    }

    Server() {
        start();
    }

    // サーバー起動
    private void start() {
        try (ServerSocketChannel serverSocketChannel = initializeServer()) {
            var selector = Selector.open();

            // サーバーソケットチャネルのAcceptイベントをセレクターに登録
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // チャネルにイベントが登録されるまで待つ
            while (selector.select() > 0) {

                // イベントを取得
                for (var key : selector.selectedKeys()) {
                    if (!key.isValid())
                        continue;

                    if (key.isAcceptable()) {
                        // Acceptイベントの場合
                        handleAccept(serverSocketChannel, selector);
                    } else if (key.isReadable()) {
                        // Readイベントの場合
                        handleRead(key);
                    }
                }

                // 処理したイベントを削除
                selector.selectedKeys().clear();
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバー停止");
        }
    }

    // 初期化されたサーバーチャンネルを取得
    private ServerSocketChannel initializeServer() throws IOException {
        var serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(HOST, PORT));
        System.out.println("チャットサーバー起動");

        return serverChannel;
    }

    // クライアントからのAcceptイベントを処理
    private void handleAccept(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        var clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannels.add(clientChannel);

        // クライアントからのReadイベントをセレクターに登録
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    // クライアントからのReadイベントを処理
    private void handleRead(SelectionKey key) throws IOException {
        var channel = (SocketChannel) key.channel();
        var buffer = ByteBuffer.allocate(1024);
        var read = channel.read(buffer);

        // クライアントからの切断を検知
        if (read == -1) {
            clientChannels.remove(channel);
            channel.close();
            key.channel();
            return;
        }

        buffer.flip();
        var message = new String(buffer.array(), 0, read);

        // クライアントからのメッセージを全てのクライアントにブロードキャスト
        broadcast(message);

        // 受け取ったメッセージはサーバー側でも表示
        System.out.println(message);
    }

    // クライアントからのメッセージを全てのクライアントにブロードキャスト
    private void broadcast(String message) throws IOException {
        for (var channel : clientChannels) {
            channel.write(ByteBuffer.wrap(message.getBytes()));
        }
    }

    // 以下Unitテスト用のメソッド
    public static Set<SocketChannel> getClientChannels() {
        return clientChannels;
    }
}