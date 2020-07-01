package com.excentro.netstorage.nettyclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class Client {

  private final String host;
  private final int    port;

  public Client(String host,
                int port) {
    this.host = host;
    this.port = port;
  }

  public static void main(String[] args) throws InterruptedException {
    new Client("127.0.0.1", 8888).start();
  }

  private void start() throws InterruptedException {
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap();
      b.group(group)
       .channel(NioSocketChannel.class)
       .remoteAddress(new InetSocketAddress(host, port))
       .handler(
           new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) {
               ChannelPipeline p = ch.pipeline();
               p.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
               p.addLast("objectEncoder", new ObjectEncoder());
               p.addLast("frameDecoder", new LineBasedFrameDecoder(8192));
               p.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
               p.addLast("objectDecoder",
                         new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
               p.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
               p.addLast("clientHandler", new ClientHandler());
               // Удаляем пока эти хендлеры
               p.remove("objectEncoder");
               p.remove("objectDecoder");
             }
           });
      b.connect()
       .sync()
       .channel()
       .closeFuture()
       .sync();
    } finally {
      group.shutdownGracefully()
           .sync();
    }
  }
}
