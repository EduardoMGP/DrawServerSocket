package gartic;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("httpServerCodec", new HttpServerCodec());

        pipeline.addLast("httpHandler", new SimpleChannelInboundHandler<>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                if (msg instanceof HttpRequest httpRequest) {
                    HttpHeaders headers = httpRequest.headers();
                    if ("Upgrade".equalsIgnoreCase(headers.get(HttpHeaderNames.CONNECTION)) &&
                            "WebSocket".equalsIgnoreCase(headers.get(HttpHeaderNames.UPGRADE))) {

                        ctx.pipeline().remove("httpHandler");
                        ctx.pipeline().addLast(new ClientHandler(ctx.channel()));
                        handleHandshake(ctx, httpRequest);
                        System.out.println("Handshake is done");
                    }
                }
            }

            private String getWebSocketURL(HttpRequest req) {
                String url = "ws://" + req.headers().get("Host") + req.uri();
                return url;
            }

            /* Do the handshaking for WebSocket request */
            private void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
                WebSocketServerHandshakerFactory wsFactory =
                        new WebSocketServerHandshakerFactory(getWebSocketURL(req), null, true);
                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), req);
                }
            }
        });
    }
}
