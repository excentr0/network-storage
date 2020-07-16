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
  private final FileHandler fileHandler;

  public FileClient(final String host, final int port, PanelController remotePanel)
      throws InterruptedException {

    NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    this.fileHandler = new FileHandler(remotePanel);
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

      b.connect(host, port).sync().channel().closeFuture().sync();
    } finally {
      eventLoopGroup.shutdownGracefully();
    }
  }

  public void run() {
    LOGGER.info("Client started");
  }

  public void sendCommand(Commands command) {
    fileHandler.sendCommand(command);
  }
}
