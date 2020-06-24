package com.excentro.netstorage.gui;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Network {
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
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                  channel = socketChannel;
                  socketChannel.pipeline().addLast(new StringDecoder(), new StringEncoder());
                }
              });
      ChannelFuture future = b.connect(HOST, PORT).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
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
