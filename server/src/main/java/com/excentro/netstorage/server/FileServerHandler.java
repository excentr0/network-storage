package com.excentro.netstorage.server;

import com.excentro.netstorage.common.FileInfo;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
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
  List<FileInfo> fileInfos = updatePath(Paths.get("D:\\"));

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    ctx.writeAndFlush("Ready");
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    LOGGER.info("Got {} from channel", msg);
    if (msg.equals("list")) {
      ctx.write(updatePath(Paths.get("D:\\")));
    } else {
      ctx.write("Ready");
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());

    if (ctx.channel().isActive()) {
      ctx.writeAndFlush(
              "Err: " + cause.getClass().getSimpleName() + ": " + cause.getMessage() + "\n")
          .addListener(ChannelFutureListener.CLOSE);
    }
  }

  private List<FileInfo> updatePath(Path path) {
    List<FileInfo> result = new ArrayList<>();
    try {
      result.addAll(list(path).map(FileInfo::new).collect(Collectors.toList()));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage());
    }
    return result;
  }

  private void sendFile(ChannelHandlerContext ctx, String msg) throws IOException {
    RandomAccessFile raf = null;
    long length = -1L;
    try {
      raf = new RandomAccessFile(msg, "r");
      length = raf.length();
    } catch (IOException e) {
      ctx.writeAndFlush("Err: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n");
    } finally {
      if (length < 0 && raf != null) {
        raf.close();
      }
    }
    if (raf != null) {
      ctx.write("OK: " + raf.length() + "\n");
      ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
    }
    ctx.writeAndFlush("\n");
  }
}
