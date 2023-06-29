package gartic.parties;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gartic.ClientHandler;
import gartic.models.Request;
import gartic.models.Response;
import gartic.models.Response.Code;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Parties {

    private static final Map<String, Party> parties = new HashMap<>();

    public static Party getParty(String name) {
        return parties.get(name);
    }

    public static void disconnectHost(ClientHandler client) {
        Party party = Parties.getPartyHost(client);
        if (party != null) {
            parties.remove(party.name());
            party.broadcast(new Response().code(Code.PARTY_CLOSED).data("party", party.name()));
            client.send(new Response().code(Code.PARTY_CLOSED));
            party.disconnectAll();
        }
    }

    public static Party getPartyHost(ClientHandler client) {
        for (Party party : parties.values()) {
            if (party.isHost(client)) {
                return party;
            }
        }
        return null;
    }

    public static void createParty(ClientHandler host) {
        Party party = Parties.getPartyHost(host);
        if (party != null) {
            host.send(new Response().code(Code.PARTY_LIMIT));
        } else {
            String name = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
            if (parties.containsKey(name))
                host.send(new Response().code(Code.PARTY_ERROR));
            else {
                parties.put(name, new Party(name, host));
                host.send(new Response().code(Code.PARTY_CREATED).data("partyName", name));
            }
        }
    }

    public static void drawing(Request request, ClientHandler client) {

        String partyName = request.data("partyName").toString();
        Party party = Parties.getParty(partyName);
        if (party != null)
            if (party.isHost(client)) {
                String chunkString = request.data("chunk").toString();
                TypeToken<List<CanvasHistory>> token = new TypeToken<>() {
                };
                List<CanvasHistory> canvasHistory = new Gson().fromJson(chunkString, token.getType());
                party.drawing(canvasHistory);
                client.send(
                        new Response().code(Code.DRAWN)
                                .data("peoples", party.peoples().size())
                );
                return;
            }
        client.send(new Response().code(Code.DRAWN_ERROR));

    }

    public static void connect(String partyName, ClientHandler client) {
        Party party;
        Code type = Code.PARTY_CONNECT_ERROR;

        if (partyName != null && (party = Parties.getParty(partyName)) != null) {
            if (party.isConnected(client)) {
                type = Code.PARTY_ALREADY_CONNECTED;
            } else {

                if (party.connect(client)) {
                    int size = party.history().size();
                    int chunkSize = Math.max((int) Math.ceil(size / 100.0), 1);

                    client.send(new Response()
                            .code(Code.PARTY_CONNECTED)
                            .data("peoples", party.peoples().size())
                    );

                    for (int i = 0; i < size; i += chunkSize) {
                        int end = Math.min(i + chunkSize, size);
                        client.send(
                                new Response()
                                        .code(Code.DRAWING_RECEIVED)
                                        .data("chunk", party.history().subList(i, end))
                                        .data("peoples", party.peoples().size())
                        );
                    }

                    party.broadcast(
                            new Response().code(Code.PARTY_CLIENT_JOINED)
                                    .data("peoples", party.peoples().size()),
                            true
                    );

                    return;
                }

                type = Code.PARTY_NOT_CONNECTED;
            }
        }

        client.send(new Response().code(type));

    }

    public static void disconnect(ClientHandler client) {
        for (Party party : parties.values()) {
            if (party.isConnected(client)) {
                if (party.disconnect(client)) {
                    client.send(
                            new Response()
                                    .code(Code.PARTY_DISCONNECTED)
                                    .data("peoples", party.peoples().size())
                    );
                    party.broadcast(new Response().code(Code.PARTY_CLIENT_LEFT)
                            .data("peoples", party.peoples().size()), true);
                    return;
                }
            }
        }
    }

    public static void disconnect(String partyName, ClientHandler client) {
        Party party;
        Code type = Code.PARTY_CONNECT_ERROR;

        if (partyName != null && (party = Parties.getParty(partyName)) != null) {
            if (party.isHost(client)) {
                Parties.disconnectHost(client);
                return;
            } else if (party.isConnected(client)) {

                if (party.disconnect(client)) {
                    client.send(
                            new Response()
                                    .code(Code.PARTY_DISCONNECTED)
                                    .data("peoples", party.peoples().size())
                    );
                    party.broadcast(new Response().code(Code.PARTY_CLIENT_LEFT)
                            .data("peoples", party.peoples().size()), true);
                    return;
                }

                type = Code.PARTY_DISCONNECT_ERROR;
            } else {
                type = Code.PARTY_DISCONNECT_ERROR;
            }
        }

        client.send(new Response().code(type));
    }

    public static void list(ClientHandler clientHandler) {
        StringBuilder json = new StringBuilder("[");
        for (Party party : parties.values())
            if (json.length() > 1)
                json.append(",{\"name\":\"").append(party.name()).append("\",\"peoples\":").append(party.peoples().size()).append("}");
            else
                json.append("{\"name\":\"").append(party.name()).append("\",\"peoples\":").append(party.peoples().size()).append("}");
        json.append("]");
        clientHandler.send(
                new Response().code(Code.PARTY_LIST)
                        .data("parties", json)
        );
    }
}
