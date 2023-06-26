package gartic.parties;

import gartic.ClientHandler;
import gartic.models.Response;

import java.util.ArrayList;
import java.util.List;

public class Party {

    private final List<ClientHandler> clients;
    private final ClientHandler host;
    private final String partyName;
    private final List<CanvasHistory> history;

    public Party(String name, ClientHandler host) {
        this.clients = new ArrayList<>();
        this.host = host;
        this.partyName = name;
        this.history = new ArrayList<>();
    }

    protected void broadcast(Response data) {
        this.broadcast(data, false);
    }

    protected void broadcast(Response data, boolean includeHost) {
        System.out.println("Broadcasting: " + data.toString());
        for (ClientHandler client : this.clients) {
            client.send(data);
        }
        if (includeHost) {
            this.host.send(data);
        }
    }

    protected void drawing(List<CanvasHistory> history) {
        this.history.addAll(history);
        this.broadcast(
                new Response()
                        .code(Response.Code.DRAWING_RECEIVED)
                        .data("chunk", history)
                        .data("peoples", this.clients.size())
        );
    }

    protected boolean disconnect(ClientHandler client) {
        return this.clients.remove(client);
    }

    protected boolean connect(ClientHandler client) {
        return this.clients.add(client);
    }

    protected boolean isConnected(ClientHandler client) {
        return this.clients.contains(client);
    }

    protected boolean isHost(ClientHandler client) {
        return this.host == client;
    }

    public String name() {
        return partyName;
    }

    public void disconnectAll() {
        this.clients.clear();
    }

    public List<CanvasHistory> history() {
        return history;
    }

    public List<ClientHandler> peoples() {
        return this.clients;
    }
}
