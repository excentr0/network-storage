package com.excentro.netstorage.nettyclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class Client {
  static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));
  private static final String HOST = "127.0.0.1";
  private static final int PORT = 8888;

  public static void main(String[] args) throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
          .channel(NioSocketChannel.class)
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(
                      new ObjectEncoder(),
                      new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                      new ChunkedWriteHandler(),
                      new ClientHandler());
                }
              });

      // Start the connection attempt.
      b.connect(HOST, PORT).sync().channel().closeFuture().sync();
    } finally {
      group.shutdownGracefully();
    }
  }
}
