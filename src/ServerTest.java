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

    // クライアントを2つサーバーと接続
    @Test
    public void testConnection() throws IOException {
        try (Socket socket1 = new Socket(Server.HOST, Server.PORT);
                Socket socket2 = new Socket(Server.HOST, Server.PORT)) {

            // クライアント側のソケットの接続確認
            assertTrue(socket1.isConnected());
            assertTrue(socket2.isConnected());

            // サーバー側のソケットの接続確認
            for (ClientHandler clientHandler : Server.handlers) {
                assertTrue(clientHandler.getSocket() != null && clientHandler.getSocket().isConnected());
            }
        }
    }
}
