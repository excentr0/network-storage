package com.excentro.netstorage.server;

import com.excentro.netstorage.commons.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
  static final Logger       LOGGER       = LoggerFactory.getLogger(FileServerHandler.class);
  private      OutputStream outputStream;
  private      int          writtenBytes = 0;
  private      byte[]       buffer       = new byte[0];

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("Got connection from {}", ctx.channel());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx,
                          Object msg) throws IOException {
    if (msg instanceof String) {
      String msg1 = (String) msg;
      if (msg1.startsWith("file")) {
        ctx.writeAndFlush(updatePath(Paths.get("D:\\Документы")));
      } else if (msg1.startsWith("download")) {
        sendFile(ctx, "D:\\tmp\\1.mp4");
      }
    }
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

  private ArrayList<FileInfo> updatePath(Path path) {
    ArrayList<FileInfo> result = new ArrayList<>();
    try {
      result.addAll(list(path).map(FileInfo::new)
                              .collect(Collectors.toList()));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage());
    }
    return result;
  }

  private void sendFile(ChannelHandlerContext ctx,
                        String fileName) throws IOException {
    final int BUFFER_SIZE = 128 * 1024; //this is actually bytes
    File file = new File(fileName);
    ctx.writeAndFlush(file.length());
    FileInputStream fis = new FileInputStream(fileName);
    byte[] tmpBuffer = new byte[BUFFER_SIZE];
    int write;
    while ((write = fis.read(tmpBuffer)) > 0) {
      if (write < BUFFER_SIZE) { // записываем последнюю часть файла, которая меньше нашего буфера
        byte[] smallBuffer = new byte[write];
        System.arraycopy(tmpBuffer, 0, smallBuffer, 0, write);
        ctx.writeAndFlush(smallBuffer);
      }
      ctx.writeAndFlush(tmpBuffer);
    }

    LOGGER.info("File {} seeded", fileName);
    fis.close();
  }

  private void sendFolderInfo(ChannelHandlerContext ctx,
                              String path) {
    List<FileInfo> fileInfoList = updatePath(Paths.get(path));
    LOGGER.info("{}", fileInfoList);
    ctx.writeAndFlush(fileInfoList);
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
