package gartic;

import com.google.gson.Gson;
import gartic.models.Request;
import gartic.parties.Parties;
import gartic.parties.Party;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler {

    private static final Gson gson = new Gson();

    private Socket socket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private Thread clientThread;

    private final Map<String, String> headers = new HashMap<>();
    private String webSocketKey;
    private String responseKey;

    /**
     * A string mágica "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
     * é definida pelo protocolo WebSocket e é usada para o
     * cálculo da chave de resposta durante o handshake.
     * Essa string é especificada no RFC 6455, que é o documento
     * de especificação do protocolo WebSocket.
     * <p>
     * A RFC 6455 define a string mágica como parte do processo
     * de geração da chave de resposta para garantir a segurança
     * e integridade da conexão WebSocket. Essa string específica é
     * usada para concatenar com a chave de autenticação enviada
     * pelo cliente antes de calcular o hash SHA-1.
     */
    private final static String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    public ClientHandler(Socket accept) {

        try {

            this.socket = accept;
            this.inputStream = new BufferedInputStream(this.socket.getInputStream());
            this.outputStream = new BufferedOutputStream(this.socket.getOutputStream());
            System.out.println("Client connected from " + this.socket.getInetAddress() + ":" + this.socket.getPort());

            if (handshakeResponse()) {

                this.clientThread = new Thread(() -> {
                    try {

//                        int len;
//                        byte[] data = new byte[1024];
//                        StringBuilder message = new StringBuilder();
//                        while ((len = this.inputStream.read(data)) != -1) {
//
//                            System.out.println(message);
//                            if (message.toString().endsWith("\n")) {
//                                Request request = gson.fromJson(message.toString(), Request.class);
//                                onMessage(request);
//                                message = new StringBuilder();
//                            }
//
//                            message.append(new String(data, 0, len));
//                        }

                        int len;
                        byte[] data = new byte[1024];
                        while ((len = this.inputStream.read(data)) != -1) {
                            decodeWebSocketFrame(data);
                        }

                    } catch (Exception e) {
                        System.out.println("Could not read from socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
                    }

                });

                this.clientThread.start();

            } else {
                System.out.println("Could not handshake with client " + this.socket.getInetAddress() + ":" + this.socket.getPort());
                this.close();
            }

        } catch (Exception e) {
            System.out.println("Could not get streams from socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
        }

    }

    private void decodeWebSocketFrame(byte[] data) {
        int index = 0;

        byte firstByte = data[index++];
        boolean isFinalFrame = (firstByte & 0x80) != 0;
        int opcode = firstByte & 0x0F;

        byte secondByte = data[index++];
        boolean hasMask = (secondByte & 0x80) != 0;
        int payloadLength = secondByte & 0x7F;

        if (payloadLength == 126) {
            // Se o payloadLength for 126, os dois próximos bytes representam o comprimento real
            payloadLength = ((data[index++] & 0xFF) << 8) | (data[index++] & 0xFF);
        } else if (payloadLength == 127) {
            // Se o payloadLength for 127, os próximos oito bytes representam o comprimento real
            // (Nota: este exemplo não lida com payloads muito grandes)
            index += 8;
        }

        byte[] mask = null;
        if (hasMask) {
            // Se o frame tiver uma máscara, os próximos quatro bytes representam a máscara
            mask = new byte[4];
            System.arraycopy(data, index, mask, 0, 4);
            index += 4;
        }

        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, index, payload, 0, payloadLength);

        // Decodificar o payload, aplicando a máscara (se houver)
        if (hasMask) {
            for (int i = 0; i < payloadLength; i++) {
                payload[i] ^= mask[i % 4];
            }
        }

        // A partir daqui, você pode trabalhar com os dados do payload decodificados
        String mensagem = new String(payload, StandardCharsets.UTF_8);
        System.out.println("Mensagem recebida: " + mensagem);
    }

    public void close() {
        try {
            this.socket.close();
            if (this.clientThread != null)
                this.clientThread.interrupt();
            System.out.println("Client disconnected from " + this.socket.getInetAddress() + ":" + this.socket.getPort());
        } catch (Exception e) {
            System.out.println("Could not close socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
        }
    }

    private void onMessage(Request request) {
        switch (request.type()) {

            case "create" -> {

                String partyName = UUID.randomUUID().toString();
                if (Parties.createParty(partyName, this))
                    send(new Request().type("created").params("partyName", partyName));
                else send(new Request().type("error").params("message", "Could not create party"));

            }

            case "drawing" -> {

                String partyName = request.param("partyName");
                Party party = Parties.getParty(partyName);
                if (party != null)
                    party.drawing(request.param("canvas"));

            }

            case "connect" -> {

                String partyName = request.param("partyName");
                Party party = Parties.getParty(partyName);
                if (party != null) {
                    party.connect(this);
                } else send(new Request().type("error").params("message", "Could not find party"));

            }

            case "disconnect" -> {

                String partyName = request.param("partyName");
                Party party = Parties.getParty(partyName);
                if (party != null) {
                    party.disconnect(this);
                } else send(new Request().type("error").params("message", "Could not find party"));

            }
        }
    }

    public void send(Request request) {
        String message = gson.toJson(request) + "\n";
//        if (connectionHeaders.size() == 0) {
//            connectionHeaders = parseHttpHeader(message);
//            handshakeResponse();
//        }

        try {
            this.outputStream.write((message + "\n").getBytes());
            this.outputStream.flush();
        } catch (Exception e) {
            System.out.println("Could not write to socket " + this.socket.getInetAddress() + ":" + this.socket.getPort() + ". Erro " + e.getMessage());
        }

    }


    /**
     * Realiza o handshake inicial com o cliente
     * Para estabelecer a conexão websocket
     *
     * @return true se o handshake foi bem sucedido
     */
    private boolean handshakeResponse() {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String requestLine = reader.readLine();
            String line;

            // Verifica se o cliente esta se conectando a partir de um websocket
            if (requestLine.equals("GET / HTTP/1.1")) {

                // Lê os headers da requisição
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    String[] parts = line.split(":");
                    if (parts.length == 2)
                        headers.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }

                String webSocketKey = headers.get("sec-websocket-key");
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] digest = md.digest((webSocketKey + GUID).getBytes());
                String responseKey = Base64.getEncoder().encodeToString(digest);

                String response = "HTTP/1.1 101 Switching Protocols\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Sec-WebSocket-Accept: " + responseKey + "\r\n\r\n";

                System.out.println("Handshake response: " + response);
                outputStream.write(response.getBytes());
                outputStream.flush();

                System.out.println("Handshake response sent to client.");
                return true;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private HashMap<String, String> parseHttpHeader(String h) {
        HashMap<String, String> fields = new HashMap<String, String>();

        String[] rows = h.split("\n");
        if (rows.length > 1) {
            fields.put("Prototcol", rows[0]);
            Pattern pattern = Pattern.compile("^([^:]+): (.+)$");
            for (int i = 1; i < rows.length; i++) {
                Matcher matcher = pattern.matcher(rows[i]);
                while (matcher.find()) {
                    if (matcher.groupCount() == 2) {
                        fields.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
        }
        return fields;
    }


}
