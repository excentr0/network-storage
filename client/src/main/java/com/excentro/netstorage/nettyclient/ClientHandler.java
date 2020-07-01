package com.excentro.netstorage.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
  static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);

  File outFile = new File("D:\\tmp\\newfile.txt");

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // Send the first message if this handler is a client-side handler.
    ByteBuf byteBuf = Unpooled.buffer();
    byteBuf = byteBuf.writeInt(2);
    ctx.writeAndFlush(byteBuf);
    LOGGER.info("{} channelActive called", ctx.channel());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    LOGGER.info("{} read complete", ctx.channel());
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());
    ctx.close();
  }


  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              ByteBuf msg) {
    LOGGER.info("{}", msg.capacity());
  }


}
