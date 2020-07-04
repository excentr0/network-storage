package com.excentro.netstorage.nettyclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;

public class Client {
  private static final boolean EPOLL = Epoll.isAvailable();

  private final String host;
  private final int    port;

  public Client(String host,
                int port) {
    this.host = host;
    this.port = port;
  }

  public static void main(String[] args) throws InterruptedException {
    new Client("127.0.0.1", 8888).start();
  }

  private void start() throws InterruptedException {
    EventLoopGroup clientGroup = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(clientGroup)
       .channel(NioSocketChannel.class)
       .handler(
           new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) {
               ChannelPipeline p = ch.pipeline();
               p.addLast(new ChunkedWriteHandler())
                .addLast("clientHandler", new ClientHandler());
             }
           });
      b.connect(host, port)
       .sync()
       .channel()
       .closeFuture()
       .sync();
    } finally {
      clientGroup.shutdownGracefully();
    }
  }
}
