import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

public class ServerTest {
    // テスト用の前処理
    @Before
    public void setUp() throws Exception {
        // サーバーを別スレッドで起動
        Executors.newSingleThreadExecutor().execute(() -> new Server());
    }

    // テスト用の後処理
    @After
    public void tearDown() throws Exception {
    }

    // クライアントとの接続テスト
    @Test
    public void testConnection() throws Exception {
        // サーバーの準備ができるまで待つ
        Thread.sleep(1000);

        // クライアントを二つサーバーと接続
        var socketChannel1 = SocketChannel.open(new InetSocketAddress(Server.HOST, Server.PORT));
        var socketChannel2 = SocketChannel.open(new InetSocketAddress(Server.HOST, Server.PORT));

        // サーバーがクライアントを受け入れるのを待つ
        Thread.sleep(1000);

        // クライアント側のソケットがisConnectedか
        assertTrue(socketChannel1.isConnected());
        assertTrue(socketChannel2.isConnected());

        // サーバー側のソケットがisConnectedか
        for (var channel : Server.clientChannels) {
            assertTrue(channel.socket() != null && channel.socket().isConnected());
        }

        socketChannel1.close();
        socketChannel2.close();
    }

    // クライアントとの接続が切れたときにClientHandlerのリソースが解放されているかのテスト
    @Test
    public void testResourceReleaseAfterClientDisconnection() throws Exception {
        // サーバーがクライアントを受け入れるのを待つ
        Thread.sleep(1000);

        // クライアントがサーバーと接続
        var socketChannel1 = SocketChannel.open(new InetSocketAddress(Server.HOST, Server.PORT));

        // サーバーがクライアントを受け入れるのを待つ
        Thread.sleep(1000);

        // サーバー側のスレッド取得
        var clientChannel = Server.clientChannels.iterator().next();

        // クライアントとの接続を切断
        socketChannel1.close();

        // clienthandlerが持つソケットがcloseされているかをテスト
        assertTrue(clientChannel.socket().isClosed());
    }
}
