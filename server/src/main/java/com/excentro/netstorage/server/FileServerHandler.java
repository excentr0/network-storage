package com.excentro.netstorage.server;

import com.excentro.netstorage.commons.Commands;
import com.excentro.netstorage.commons.Dir;
import com.excentro.netstorage.commons.FileInfo;
import com.excentro.netstorage.commons.SrcDst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.excentro.netstorage.commons.FileActions.*;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileServerHandler.class);
  private String fileName;
  private long fileSize;
  private String localPath = "D:\\tmp";
  private Path dstPath = Paths.get(localPath);
  private Path srcPath;

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("Got connection from {}", ctx.channel());
    File saveDir = new File(localPath);
    if (!saveDir.exists()) {
      saveDir.mkdirs();
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
    ByteBuf buf = ctx.alloc().buffer();
    try {
      if (msg instanceof byte[]) { // принимаем файл
        buf.writeBytes((byte[]) msg);
        saveFile(buf, fileSize, dstPath);
      } else if (msg instanceof Commands) {
        switch ((Commands) msg) {
          case UPLOAD:
            LOGGER.info("Ready to receive file");
            ctx.writeAndFlush("Ready");
            break;
          case DOWNLOAD:
            LOGGER.info("Sending file");
            sendFile(ctx, srcPath, dstPath);
            break;
          case MOVE:
          case DELETE:
            break;
          case DIR:
            Dir dir = updatePath(dstPath.getParent());
            ctx.writeAndFlush(dir);
            LOGGER.info("Sending folder info: {}", dir);
            break;
          default:
            LOGGER.error("Unexpected value: {}", msg);
        }
      } else if (msg instanceof FileInfo) {
        fileName = localPath.concat(((FileInfo) msg).getFilename());
        fileSize = ((FileInfo) msg).getSize();
        LOGGER.info("Got FileInfo: {}", msg);
      } else if (msg instanceof SrcDst) {
        LOGGER.info("Got path {}", msg);
        dstPath = Paths.get(((SrcDst) msg).getDst());
        srcPath = Paths.get(((SrcDst) msg).getSrc());
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
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error(cause.getMessage());

    if (ctx.channel().isActive()) {
      ctx.writeAndFlush(
              "Err: " + cause.getClass().getSimpleName() + ": " + cause.getMessage() + "\n")
          .addListener(ChannelFutureListener.CLOSE);
    }
  }
}
