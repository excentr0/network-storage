package com.excentro.netstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerApp {
  public static void main(String[] args) {
    EventLoopGroup boosGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(workerGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(
              new ChannelInitializer<SocketChannel>() {
                protected void initChannel(final SocketChannel socketChannel) throws Exception {
                  socketChannel.pipeline().addLast(new MainHandler());
                }
              });
      ChannelFuture future = b.bind(8888).sync();
      future.channel().closeFuture().sync();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      boosGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
