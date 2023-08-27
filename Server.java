import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// クライアントからの入力を受け取って、サーバーと接続しているクライアント全員にブロードキャストするスレッド
class Worker extends Thread {
    private Socket socket;
    private PrintWriter writer;

    Worker(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (InputStream in = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                PrintWriter tempWriter = new PrintWriter(socket.getOutputStream(), true);
                Socket tempSocket = socket) {

            writer = tempWriter;
            synchronized (Server.writers) {
                Server.writers.add(writer);
            }

            // クライアントからの投稿を待ち受ける
            String line;
            while ((line = reader.readLine()) != null) {

                // 接続している全てのクライアントにブロードキャスト
                synchronized (Server.writers) {
                    for (PrintWriter pw : Server.writers) {
                        pw.println(line);
                    }
                }

                // 受け取ったメッセージはサーバー側でも表示
                System.out.println(line);
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            // サーバーが持つprintwriter一覧から自身のprintwriterを削除
            synchronized (Server.writers) {
                Server.writers.remove(writer);
            }
        }
    }
}

// クライアントからの接続を待ち受ける
public class Server {
    // サーバーと接続しているクライアントとの出力ストリームを保持
    public static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(1234)) {

            System.out.println("チャットサーバー起動");

            // クライアントからの接続を確認するたびにworkerスレッド生成
            while (true) {
                Socket socket = serverSocket.accept();
                new Worker(socket).start();
            }

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバー停止");
        }
    }
}