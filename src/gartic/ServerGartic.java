package gartic;

import java.net.ServerSocket;

public class ServerGartic {

    public static void main(String[] args) {
        new ServerGartic(8080);
    }

    private ServerSocket serverSocket;
    public ServerGartic(int port) {

        try {

            this.serverSocket = new ServerSocket(port);
            System.out.println("Server started. Listening on port: " + port);
            new Thread(() -> {
                while (true) {
                    try {
                        new ClientHandler(this.serverSocket.accept());
                    } catch (Exception e) {
                        System.out.println("Could not accept client. Erro " + e.getMessage());
                    }
                }
            }).start();

        } catch (java.io.IOException e) {
            System.out.println("Could not listen on port: " + port);
            System.exit(-1);
        }

    }

}
