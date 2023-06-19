package gartic.parties;

import gartic.ClientHandler;
import gartic.models.Request;

import java.util.ArrayList;
import java.util.List;

public class Party {

    private final List<ClientHandler> clients;
    private final ClientHandler host;
    private final String partyName;
    private String canvas;

    public Party(String name, ClientHandler host) {
        this.clients = new ArrayList<>();
        this.host = host;
        this.partyName = name;
    }

    private void broadcast(Request data) {
        for (ClientHandler client : this.clients) {
            client.send(data);
        }
    }

    public void drawing(String canvas) {
        this.canvas = canvas;
        this.broadcast(
                new Request()
                        .type("drawing")
                        .params("partyName", partyName)
                        .params("canvas", canvas)
        );
    }

    public void disconnect(ClientHandler client) {
        this.clients.remove(client);
        client.close();
        client.send(
                new Request()
                        .type("disconnected")
                        .params("partyName", partyName)
        );
    }

    public void connect(ClientHandler client) {
        this.clients.add(client);
        client.send(
                new Request()
                        .type("connected")
                        .params("partyName", partyName)
                        .params("canvas", canvas)
        );
    }
}
