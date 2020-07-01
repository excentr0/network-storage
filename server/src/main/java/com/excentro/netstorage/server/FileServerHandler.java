package com.excentro.netstorage.server;

import com.excentro.netstorage.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws IOException {
    LOGGER.info("{} channel active", ctx.channel());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx,
                          Object msg) {
    LOGGER.info("{} read from channel", msg.getClass());
    ByteBuf byteBuf = (ByteBuf) msg;
    if (byteBuf.readInt() == 1) {
      ctx.writeAndFlush(updatePath(Paths.get("D:\\tmp")));
      LOGGER.info("Sending dir");
    } else if (byteBuf.readInt() == 2) {
      try (RandomAccessFile raf = new RandomAccessFile("D:\\tmp\\logo.jpg", "r")) {
        ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
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

    if (ctx.channel()
           .isActive()) {
      ctx.writeAndFlush(
          "Err: " + cause.getClass()
                         .getSimpleName() + ": " + cause.getMessage() + "\n")
         .addListener(ChannelFutureListener.CLOSE);
    }
  }

  private List<FileInfo> updatePath(Path path) {
    List<FileInfo> result = new ArrayList<>();
    try {
      result.addAll(list(path).map(FileInfo::new)
                              .collect(Collectors.toList()));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage());
    }
    return result;
  }

  private void sendFile(ChannelHandlerContext ctx) {
    Channel channel = ctx.channel();
    channel.write(Unpooled.copiedBuffer("Netty", CharsetUtil.UTF_8));
  }
}
