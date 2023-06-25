package gartic;

import com.google.gson.Gson;
import gartic.models.Request;
import gartic.models.Response;
import gartic.parties.Parties;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.util.LinkedList;
import java.util.List;

public class ClientHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Gson gson = new Gson();
    private static final List<Channel> clients = new LinkedList<>();
    private Channel chann;

    public ClientHandler(Channel chann) {
        this.chann = chann;
        clients.add(this.chann);
        System.out.println("Novo cliente conectado: " + this.chann.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (msg instanceof TextWebSocketFrame message) {
            System.out.println("Recebido: " + message.text());
            Request request = gson.fromJson(message.text(), Request.class);
            switch (request.action()) {

                case PARTY_CREATE -> {
                    System.out.println("Recebido pedido de criação de uma sala");
                    Parties.createParty(this);
                }

                case DRAWING -> {
                    System.out.println("Recebido desenho de uma sala");
                    Parties.drawing(request, this);
                }

                case PARTY_CONNECT -> {
                    System.out.println("Recebido pedido de conexão a uma sala");
                    Parties.connect(request.data("partyName").toString(), this);
                }

                case PARTY_DISCONNECT -> {
                    System.out.println("Recebido pedido de desconexão de uma sala");
                    Parties.disconnect(request.data("partyName").toString(), this);
                }

                case PARTY_LIST -> {
                    System.out.println("Recebido pedido de listagem de salas");
                    Parties.list(this);
                }

                default -> System.out.println("Recebido pedido desconhecido");

            }
        }
    }

    public void send(Response response) {
        this.chann.writeAndFlush(new TextWebSocketFrame(gson.toJson(response)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // Cliente desconectado, remove o canal da lista de clientes
        Channel clientChannel = ctx.channel();
        clients.remove(clientChannel);
        System.out.println("Cliente desconectado: " + clientChannel.remoteAddress());
        Parties.disconnectHost(this);
    }

}
