package com.excentro.netstorage.server;

import com.excentro.netstorage.server.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;

public class FileServerHandler extends SimpleChannelInboundHandler<String> {
  static final Logger       LOGGER       = LoggerFactory.getLogger(FileServerHandler.class);
  private      OutputStream outputStream;
  private      int          writtenBytes = 0;
  private      byte[]       buffer       = new byte[0];

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    ctx.writeAndFlush("Hello: Type the path.\n");
    LOGGER.info("Got connection from {}", ctx.channel());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
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

  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              String msg) throws IOException {
    LOGGER.info("Got {}", msg);
    sendFile(ctx, msg);
  }

  private void sendFile(ChannelHandlerContext ctx,
                        String fileName) throws IOException {
    RandomAccessFile raf = null;
    long length = -1L;
    try {
      raf    = new RandomAccessFile(fileName, "r");
      length = raf.length();
    } catch (IOException e) {
      ctx.writeAndFlush("Err: " + e.getClass()
                                   .getSimpleName() + ": " + e.getMessage() + "\n");
    } finally {
      if (length < 0 && raf != null) {
        raf.close();
      }
    }
    if (raf != null) {
      ctx.write("OK: " + raf.length() + "\n");
      ctx.write(new ChunkedFile(raf, 8192));
    }
    ctx.writeAndFlush("\n");
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

  private void saveFile(ByteBuf byteBuf) throws IOException {
    File file = new File("D:\\tmp\\4.mp4");
    if (this.outputStream == null) {
      if (Files.exists(file.toPath())) {
        Files.delete(file.toPath());
      }
      this.outputStream =
          Files.newOutputStream(file.toPath(),
                                StandardOpenOption.CREATE_NEW,
                                StandardOpenOption.APPEND);
    }

    int size = byteBuf.readableBytes();
    if (size > this.buffer.length) {
      this.buffer = new byte[size];
    }
    byteBuf.readBytes(this.buffer, 0, size);

    this.outputStream.write(this.buffer, 0, size);
    this.writtenBytes += size;

    if (writtenBytes == 627417589) {
      outputStream.close();
      LOGGER.info("Written {} bytes", writtenBytes);
    }
    LOGGER.info("Written {} bytes", writtenBytes);
  }
}
