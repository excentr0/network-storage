package com.excentro.netstorage.gui;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Network {
  static final Logger LOGGER = LoggerFactory.getLogger(Network.class);
  private static final String HOST = "127.0.0.1";
  private static final int PORT = 8888;
  private SocketChannel channel;

  public Network() {
    new Thread(this::run).start();
  }

  /** Запускаем сетевой клиент в отдельном потоке */
  private void run() {
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(workerGroup)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                  channel = socketChannel;
                  socketChannel.pipeline().addLast(new FileHandler());
                }
              });
      ChannelFuture future = b.connect(HOST, PORT).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      LOGGER.error(e.getLocalizedMessage());
    } finally {
      workerGroup.shutdownGracefully();
    }
  }

  /**
   * Метод отправки файла
   *
   * @param data пересылаемые данные
   */
  public void sendFile(String data) {
    channel.writeAndFlush(data);
  }
}
