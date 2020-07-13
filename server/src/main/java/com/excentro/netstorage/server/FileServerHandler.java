package com.excentro.netstorage.server;

import com.excentro.netstorage.commons.Commands;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static com.excentro.netstorage.commons.FileActions.*;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("Got connection from {}", ctx.channel());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx,
                          Object msg) throws IOException {
    ByteBuf buf = ctx.alloc()
                     .buffer();
    try {
      if (msg instanceof byte[]) { // принимаем файл
        buf.writeBytes((byte[]) msg);
        saveFile(buf, "D:\\tmp\\upload.mp4", 627417589); //надо передавать размер файла
      } else if (msg instanceof Commands) {
        switch ((Commands) msg) {
          case UPLOAD:
            LOGGER.info("Ready to receive file");
            ctx.writeAndFlush("Ready");
            break;
          case DOWNLOAD:
            LOGGER.info("Sending file");
            sendFile(ctx, "D:\\tmp\\1.mp4");
            break;
          case MOVE:
          case DELETE:
            break;
          case DIR:
            LOGGER.info("Sending folder info");
            ctx.writeAndFlush(updatePath(Paths.get("D:\\Документы")));
            break;
          default:
            LOGGER.error("Unexpected value: {}", msg);
        }
      }
    } finally {
      buf.release();
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());

    if (ctx.channel()
           .isActive()) {
      ctx.writeAndFlush(
          "Err: " + cause.getClass()
                         .getSimpleName() + ": " + cause.getMessage() + "\n")
         .addListener(ChannelFutureListener.CLOSE);
    }
  }
}
