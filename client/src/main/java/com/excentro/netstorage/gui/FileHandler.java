package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;


public class FileHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
  long fileSize = 0;
  private OutputStream outputStream;
  private int          writtenBytes = 0;
  private byte[]       buffer       = new byte[0];

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
//    ctx.writeAndFlush("d:\\tmp\\1.mp4");
    ctx.writeAndFlush("file");

  }

  @Override
  public void channelRead(ChannelHandlerContext ctx,
                          Object msg) throws IOException {
    LOGGER.info("Got {}",
                msg.getClass()
                   .getSimpleName());
    ByteBuf buf = ctx.alloc()
                     .buffer();
    try {
      if (msg instanceof String) {
        LOGGER.info(String.valueOf(msg));
      } else if (msg instanceof Long) {
        fileSize = (long) msg;
        LOGGER.info("File size is {}", fileSize);
      } else if (msg instanceof byte[]) {
        buf.writeBytes((byte[]) msg);
        saveFile(buf);
      }
    } finally {
      buf.release();
    }
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
    try {
      this.outputStream.write(this.buffer, 0, size);
      this.writtenBytes += size;
      if (writtenBytes == fileSize) {
        outputStream.close();
        LOGGER.info("Written {} bytes, file size - {}", writtenBytes, fileSize);
      }
    } catch (ClosedChannelException ignored) {

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
}
