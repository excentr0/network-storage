package com.excentro.netstorage.server;

import com.excentro.netstorage.common.FileInfo;
import io.netty.channel.*;
import io.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;

public class FileServerHandler extends SimpleChannelInboundHandler<ChunkedFile> {
  static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    ctx.writeAndFlush("Ready");
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

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ChunkedFile msg) throws Exception {
    long length = -1L;
    ChannelFuture sendFileFuture = null;
    try (RandomAccessFile raf = new RandomAccessFile("D:\\tmp\\file.txt", "r")) {
      length = raf.length();
      sendFileFuture =
          ctx.writeAndFlush(
              new DefaultFileRegion(raf.getChannel(), 0, length), ctx.newProgressivePromise());
      if (sendFileFuture != null) {
        sendFileFuture.addListener(
            new ChannelProgressiveFutureListener() {
              @Override
              public void operationProgressed(
                  ChannelProgressiveFuture future, long progress, long total) throws Exception {
                if (total < 0) {
                  LOGGER.info("{} Transfer progress: {}", future.channel(), progress);
                } else {
                  LOGGER.info("{} Transfer progress: {}/{}", future.channel(), progress, total);
                }
              }

              @Override
              public void operationComplete(ChannelProgressiveFuture future) throws Exception {
                LOGGER.info("{} Transfer complete", future.channel());
              }
            });
      }
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

  private void sendFile(ChannelHandlerContext ctx) throws Exception {
    Channel ch = ctx.channel();
    ch.write(new ChunkedFile(new File("D:\\tmp\\file.txt")));
  }
}
