import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Client {
    private Socket socket;

    public static void main(String[] args) {
        new Client();
    }

    Client() {
        initialize();
    }

    private void initialize() {
        try (Socket socket = new Socket(Server.HOST, Server.PORT);
                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, true);
                Scanner scanner = new Scanner(System.in)) {

            this.socket = socket;

            // ユーザー名の代わりにプロセスIDを使用
            var PID = ProcessHandle.current().pid();
            System.out.println("チャットサーバーと接続(PID: " + PID + ")");

            // サーバーからのブロードキャストを待ち受けるスレッドを起動
            Executors.newSingleThreadExecutor().execute(new ServerListener(socket));

            String input;
            // 入力を待ち受ける
            while (true) {

                // 空行が入力されたら終了
                input = scanner.nextLine();
                if (input == null || input.equals("")) {
                    break;
                }

                // サーバーにチャット送信
                var message = PID + ": " + input;
                writer.println(message);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("チャットサーバーとの接続を停止");
        }
    }

    // テスト用のメソッド
    public boolean isConnectedTest() {
        return socket != null && socket.isConnected();
    }
}

// サーバーからのブロードキャストを待ち受けるスレッド
class ServerListener implements Runnable {
    private Socket socket;

    ServerListener(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // サーバーからのメッセージを待ち受ける
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
