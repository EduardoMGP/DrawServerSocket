package gartic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerGartic {

    public static void main(String[] args) throws InterruptedException, UnknownHostException {
        if (args.length != 2)
            new ServerGartic("localhost", 8086);
        else
            new ServerGartic(args[0], Integer.parseInt(args[1]));
    }

    public ServerGartic(String ip, int port) throws InterruptedException, UnknownHostException {
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
