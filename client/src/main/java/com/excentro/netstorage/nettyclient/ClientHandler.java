package com.excentro.netstorage.nettyclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // Send the first message if this handler is a client-side handler.
    ctx.writeAndFlush("list");
    LOGGER.info("channelActive called");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
    LOGGER.info("Got {}", msg);
    if (msg.equals("Ready")) {
      Thread.sleep(1000);
    }
    ctx.write("OK");
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}
