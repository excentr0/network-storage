package com.excentro.netstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
  public static final  int     PORT   = 8888;
  static final         Logger  LOGGER = LoggerFactory.getLogger(Server.class);
  private static final boolean EPOLL  = Epoll.isAvailable();
  private final        int     port;

  public Server(int port) {
    this.port = port;
  }

  public static void main(String[] args) {
    new Server(PORT).start();
  }

  private void start() {
    EventLoopGroup bossGroup = EPOLL ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
       .channel(NioServerSocketChannel.class)
       .option(ChannelOption.SO_BACKLOG, 100)
       .childHandler(
           new ChannelInitializer<SocketChannel>() {
             public void initChannel(SocketChannel ch) {
               ChannelPipeline pipeline = ch.pipeline();
               pipeline.addLast(new ChunkedWriteHandler())
                       .addLast("serverHandler", new ServerHandler());
             }
           });
      b.bind(port)
       .sync()
       .channel()
       .closeFuture()
       .syncUninterruptibly();
      LOGGER.info("Server started on port 8888");
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
