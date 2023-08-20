import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// サーバーからのブロードキャストを待ち受けるスレッド
class ClientWorker extends Thread {
    private Socket socket;

    ClientWorker(Socket socket) {
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

// サーバーに接続してチャットを行う
public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1234);
                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, true);
                Scanner scanner = new Scanner(System.in)) {

            // ユーザー名の代わりにプロセスIDを使用
            var PID = ProcessHandle.current().pid();

            System.out.println("チャットサーバーと接続(PID: " + PID + ")");

            // サーバーからのブロードキャストを待ち受けるスレッドを起動
            new ClientWorker(socket).start();

            String input;
            // 入力を待ち受ける
            while (true) {
                input = scanner.nextLine();
                // 空行が入力されたら終了
                if (input == null || input.equals("")) {
                    break;
                }

                var message = PID + ": " + input;
                // サーバーにチャット送信
                writer.println(message);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("hello");
        } finally {
            System.out.println("チャットサーバーとの接続を停止");
        }
    }
}