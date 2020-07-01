package com.excentro.netstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {
  static final Logger LOGGER = LoggerFactory.getLogger(ServerApp.class);

  private int port;

  public ServerApp(int port) {
    this.port = port;
  }

  public static void main(String[] args) {
    new ServerApp(8888).start();
  }

  private void start() {
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
               ChannelPipeline pipeline = ch.pipeline();
               pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
               pipeline.addLast("objectEncoder", new ObjectEncoder());
               pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(8192));
               pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
               pipeline.addLast("objectDecoder",
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
               pipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
               pipeline.addLast("fileServerHandler", new FileServerHandler());
               // Удаляем пока эти хендлеры
               pipeline.remove("objectEncoder");
               pipeline.remove("objectDecoder");

             }
           });
      b.bind(port)
       .sync()
       .channel()
       .closeFuture()
       .sync();
      LOGGER.info("Server started on port 8888");
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
