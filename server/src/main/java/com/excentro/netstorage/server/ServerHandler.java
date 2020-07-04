package com.excentro.netstorage.server;

import com.excentro.netstorage.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.LastHttpContent;
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

public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
  // TODO надо как-то передавать размер файла для скачки и закачки и путь к файлу
  static final Logger       LOGGER       = LoggerFactory.getLogger(ServerHandler.class);
  private      OutputStream outputStream;
  private      int          writtenBytes = 0;
  private      byte[]       buffer       = new byte[0];

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws IOException {
    LOGGER.info("{} channel active", ctx.channel());
//    uploadFile(ctx, Paths.get("D:\\tmp\\1.mp4"));
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());
  }

  private void uploadFile(ChannelHandlerContext ctx,
                          Path path) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
    LOGGER.info("File length {} bytes", raf.length());
    ctx.writeAndFlush(new DefaultFileRegion(raf.getChannel(), 0, raf.length()));
    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              ByteBuf byteBuf) throws IOException {
    LOGGER.info("GOT {}", byteBuf);
    saveFile(byteBuf);

  }

  private void saveFile(ByteBuf byteBuf) throws IOException {
    File file = new File("D:\\tmp\\4.mp4");
//    LOGGER.info("Got {}", byteBuf);
    if (this.outputStream == null) {
//      Files.createDirectories(this.path.getParent());
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
