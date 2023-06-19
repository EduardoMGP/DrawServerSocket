package gartic;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientGartic {

    public static void main(String[] args) throws IOException {
        new ClientGartic("localhost", 8080);
    }

    private Socket socket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    public ClientGartic(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = new BufferedInputStream(socket.getInputStream());
        outputStream = new BufferedOutputStream(socket.getOutputStream());

        new Thread(() -> {
            while (true) {
                try {
                    int read = this.inputStream.read();
                    System.out.println("Read " + read);
                } catch (Exception e) {
                    System.out.println("Could not read from socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
                    break;
                }
            }
        }).start();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                send("Hello");
            }
        };

        new Timer().schedule(task, 0, 1000);
    }

    public void send(String message) {
        try {
            this.outputStream.write((message + "\n").getBytes());
            this.outputStream.flush();
        } catch (Exception e) {
            System.out.println("Could not write to socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
        }
    }

}
