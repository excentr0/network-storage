package com.excentro.netstorage.gui;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileClient {
  public static final int    port   = 8888;
  public static final String host   = "127.0.0.1";
  static final        Logger LOGGER = LoggerFactory.getLogger(FileClient.class);

  public static void main(String[] args) throws InterruptedException {
    EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(eventLoopGroup)
       .channel(NioSocketChannel.class)
       .handler(new ChannelInitializer<SocketChannel>() {
         @Override
         public void initChannel(SocketChannel ch) {
           ChannelPipeline p = ch.pipeline();
           p.addLast(
               new ObjectEncoder(),
               new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
               new FileHandler());
         }
       });
      b.connect(host, port)
       .sync()
       .channel()
       .closeFuture()
       .syncUninterruptibly();
    } finally {
      eventLoopGroup.shutdownGracefully();
    }
  }
}
