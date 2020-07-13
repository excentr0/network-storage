package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.Commands;
import com.excentro.netstorage.commons.FileActions;
import com.excentro.netstorage.commons.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;


public class FileHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
  long fileSize = 0;


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
//    ctx.writeAndFlush("d:\\tmp\\1.mp4");
    LOGGER.info("Established connection {}", ctx.channel());
    ctx.writeAndFlush(Commands.DIR);
  }


  @Override
  public void channelRead(ChannelHandlerContext ctx,
                          Object msg) {
    ByteBuf buf = ctx.alloc()
                     .buffer();
    try {
      if (msg instanceof String) {
        LOGGER.info(String.valueOf(msg));
        if (((String) msg).startsWith("Ready")) {
          FileActions.sendFile(ctx, "D:\\tmp\\1.mp4");
        }
      } else if (msg instanceof Long) {
        fileSize = (long) msg;
        LOGGER.info("File size is {}", fileSize);
      } else if (msg instanceof byte[]) {
        buf.writeBytes((byte[]) msg);
        FileActions.saveFile(buf, "D:\\tmp\\4.mp4", 627417589);
      } else if (msg instanceof ArrayList) {
        ArrayList<FileInfo> files = (ArrayList) msg;
        for (Object file : files) {
          LOGGER.info("File info: {}", file);
        }
        ctx.writeAndFlush("download");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      buf.release();
    }
  }
}
