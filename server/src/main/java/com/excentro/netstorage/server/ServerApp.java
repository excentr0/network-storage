package com.excentro.netstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {
  public static final int INET_PORT = 8888;
  static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);

  public static void main(String[] args) {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .handler(new LoggingHandler(LogLevel.INFO))
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                public void initChannel(SocketChannel ch) {
                  ch.pipeline()
                      .addLast(
                          //                          new StringEncoder(CharsetUtil.UTF_8),
                          //                          new LineBasedFrameDecoder(8192),
                          //                          new StringDecoder(CharsetUtil.UTF_8),
                          //                          new ChunkedWriteHandler(),
                          new ObjectEncoder(),
                          new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                          new FileServerHandler());
                }
              });
      b.bind(INET_PORT).sync().channel().closeFuture().sync();
      LOGGER.info("Server started on port 8888");
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
