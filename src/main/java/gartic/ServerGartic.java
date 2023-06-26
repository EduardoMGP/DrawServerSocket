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
        new ServerGartic(8080);
    }

    public ServerGartic(int port) throws InterruptedException, UnknownHostException {
        EventLoopGroup group  = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer());

        ChannelFuture channelFuture = serverBootstrap.bind(InetAddress.getByName("192.168.0.12"), port).sync();
        channelFuture.channel().closeFuture().sync();
    }

}
