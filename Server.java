import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                PrintWriter tempWriter = new PrintWriter(socket.getOutputStream(), true);
                Socket tempSocket = socket) {

            writer = tempWriter;

            String message;
            // クライアントからの投稿を待ち受ける
            while ((message = reader.readLine()) != null) {
                // 接続している全てのクライアントにブロードキャスト
                broadcast(message);
                // 受け取ったメッセージはサーバー側でも表示
                System.out.println(message);
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            // クライアントが切断したらハンドラを削除
            Server.handlers.remove(this);
        }
    }

    void broadcast(String message) {
        for (var handler : Server.handlers) {
            handler.writer.println(message);
        }
    }
}

// チャットサーバー
public class Server {

    // スレッドセーフなハッシュセット
    public static Set<ClientHandler> handlers = ConcurrentHashMap.newKeySet();

    // スレッドプール
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {

            System.out.println("チャットサーバー起動");

            // クライアントからの接続を確認するたびにハンドラを生成、スレッドプールに渡す
            while (true) {
                var socket = serverSocket.accept();
                var clientHandler = new ClientHandler(socket);
                handlers.add(clientHandler);
                executorService.execute(clientHandler);
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバー停止");
        }
    }
}