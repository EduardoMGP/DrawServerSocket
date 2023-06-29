package gartic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;

import javax.net.ssl.SSLException;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerGartic {

    public static void main(String[] args) throws InterruptedException, UnknownHostException, SSLException {
        if (args.length != 2)
            new ServerGartic("localhost", 8086);
        else
            new ServerGartic(args[0], Integer.parseInt(args[1]));
    }

    public static SslContext sslContext;
    public ServerGartic(String ip, int port) throws InterruptedException, UnknownHostException, SSLException {

        File keyCertChain = new File("./key_cert_chain.pem");
        File key = new File("./key.pem");

        if (keyCertChain.exists() && key.exists()) {
            System.out.println("Utilizando certificado SSL");
            sslContext = SslContextBuilder.forServer(keyCertChain, key)
                    .build();
        }

        System.out.println("ServerGartic running on " + ip + ":" + port);
        EventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer());

        ChannelFuture channelFuture = serverBootstrap.bind(InetAddress.getByName(ip), port).sync();
        channelFuture.channel().closeFuture().sync();
    }

}
