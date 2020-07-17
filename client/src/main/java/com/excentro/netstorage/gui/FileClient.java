package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.Commands;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileClient implements Runnable {

  static final Logger LOGGER = LoggerFactory.getLogger(FileClient.class);
  private final String host;
  private final int port;
  private final PanelController remotePC;
  private FileHandler fileHandler;

  public FileClient(String host, int port, PanelController remotePC) {
    this.host = host;
    this.port = port;
    this.remotePC = remotePC;
  }

  public void run() {
    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    this.fileHandler = new FileHandler(remotePC);
    try {
      Bootstrap b = new Bootstrap();
      b.group(eventLoopGroup)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(
                      new ObjectEncoder(),
                      new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                      fileHandler);
                }
              });

      b.connect(host, port).sync().channel().closeFuture().syncUninterruptibly();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      eventLoopGroup.shutdownGracefully();
    }
    LOGGER.info("Client started");
  }
  /** Остановить поток */
  public void stop() {}

  public void sendCommand(Commands command) {
    fileHandler.sendCommand(command);
  }
}
