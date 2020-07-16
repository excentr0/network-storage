package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.Commands;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileClient {
  static final Logger LOGGER = LoggerFactory.getLogger(FileClient.class);
  private final FileHandler fileHandler;
  private final NioEventLoopGroup eventLoopGroup;
  private final ChannelFuture channelFuture;

  public FileClient(
      final String host, final int port, final VBox localFiles, final VBox remoteFiles)
      throws InterruptedException {
    this.eventLoopGroup = new NioEventLoopGroup();
    this.fileHandler = new FileHandler(localFiles, remoteFiles);

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

    this.channelFuture = b.connect(host, port).sync().channel().closeFuture();
  }

  public void run() throws InterruptedException {
    LOGGER.info("Client started");
    channelFuture.sync();
  }

  public void stop() {
    LOGGER.info("Stopping client");
    eventLoopGroup.shutdownGracefully();
  }

  public void sendCommand(Commands command) {
    fileHandler.sendCommand(command);
  }
}
