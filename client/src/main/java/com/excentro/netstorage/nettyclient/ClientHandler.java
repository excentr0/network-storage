package com.excentro.netstorage.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

  static final Logger       LOGGER       = LoggerFactory.getLogger(ClientHandler.class);
  private      OutputStream outputStream;
  private      int          writtenBytes = 0;
  private      byte[]       buffer       = new byte[0];

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("Channel active: {}", ctx.channel());
    //    uploadFile(ctx, Paths.get("D:\\tmp\\2.mp4"));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (this.outputStream != null) {
      this.outputStream.close();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error("Error: {}", cause.getLocalizedMessage());
  }

  private void uploadFile(ChannelHandlerContext ctx, Path path) throws IOException {
    try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
      LOGGER.info("File length {} bytes", raf.length());
      ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
      ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
    //    saveFile(byteBuf);
    LOGGER.info("length - {}", byteBuf.readLong());
    LOGGER.info(
        "date - {}",
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(byteBuf.readLong()), TimeZone.getDefault()
                                                               .toZoneId()));
    String filename = byteBuf.toString(StandardCharsets.UTF_8);
    LOGGER.info("Got {} file name", filename);
  }

  private void saveFile(ByteBuf byteBuf) throws IOException {
    File file = new File("D:\\tmp\\3.mp4");
    if (this.outputStream == null) {
      if (Files.exists(file.toPath())) {
        Files.delete(file.toPath());
      }
      this.outputStream =
          Files.newOutputStream(
              file.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
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
