package com.excentro.netstorage.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
  public static final String NEW_FILE = "D:\\tmp\\newfile.txt";
  static final        Logger LOGGER   = LoggerFactory.getLogger(ClientHandler.class);
  File outFile = new File(NEW_FILE);

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("Channel active: {}", ctx.channel());
    ByteBuf firstMessage = Unpooled.buffer(10);
    for (int i = 0; i < 10; i++) {
      firstMessage.writeInt(i);
    }
    ctx.writeAndFlush(firstMessage);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error("Error: {}", cause.getLocalizedMessage());
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              ByteBuf msg) {
    LOGGER.info("Got {}", msg);
    ctx.write(msg);
  }

}
