import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.Socket;
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
        // クライアントを二つサーバーと接続
        var socket1 = new Socket(Server.HOST, Server.PORT);
        var socket2 = new Socket(Server.HOST, Server.PORT);

        // クライアント側のソケットがisConnectedか
        assertTrue(socket1.isConnected());
        assertTrue(socket2.isConnected());

        // サーバー側のソケットがisConnectedか
        for (ClientHandler clientHandler : Server.handlers) {
            assertTrue(clientHandler.getSocket() != null && clientHandler.getSocket().isConnected());
        }

        socket1.close();
        socket2.close();
    }

    // クライアントとの接続が切れたときにClientHandlerのリソースが解放されているかのテスト
    @Test
    public void testResourceReleaseAfterClientDisconnection() throws Exception {
        // クライアントがサーバーと接続
        var clientSocket = new Socket(Server.HOST, Server.PORT);

        // サーバーがクライアントを受け入れるのを待つ
        Thread.sleep(1000);

        // サーバー側のスレッド取得
        var clientHandler = Server.handlers.iterator().next();

        // クライアントとの接続を切断
        clientSocket.close();

        // clienthandlerが持つソケットがcloseされているかをテスト
        assertTrue(clientHandler.getSocket().isClosed());
    }
}
