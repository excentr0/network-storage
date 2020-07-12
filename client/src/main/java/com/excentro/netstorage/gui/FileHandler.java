package com.excentro.netstorage.gui;

import com.excentro.netstorage.gui.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;


public class FileHandler extends SimpleChannelInboundHandler<String> {
  static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
  OutputStream outputStream = null;
  byte[]       buffer       = null;
  int          writtenBytes = 0;

  /**
   * Is called for each message of type {@link I}.
   *
   * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
   *            belongs to
   * @param msg the message to handle
   * @throws Exception is thrown if an error occurred
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              String msg) throws Exception {
    saveFile(Unpooled.wrappedBuffer(msg.getBytes("UTF-8")));
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

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    ctx.writeAndFlush("d:\\tmp\\1.mp4");
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
