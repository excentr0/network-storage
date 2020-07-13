package com.excentro.netstorage.commons;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
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

public class FileActions {
  static final   Logger       LOGGER       = LoggerFactory.getLogger(FileActions.class);
  private static OutputStream outputStream;
  private static int          writtenBytes = 0;
  private static byte[]       buffer       = new byte[0];

  private FileActions() {
  }

  public static void saveFile(ByteBuf byteBuf,
                              String fileName,
                              long fileSize) throws IOException {
    File file = new File(fileName);
    if (outputStream == null) {
      if (Files.exists(file.toPath())) {
        Files.delete(file.toPath());
      }
      outputStream =
          Files.newOutputStream(file.toPath(),
                                StandardOpenOption.CREATE_NEW,
                                StandardOpenOption.APPEND);
    }
    int size = byteBuf.readableBytes();
    if (size > buffer.length) {
      buffer = new byte[size];
    }
    byteBuf.readBytes(buffer, 0, size);
    try {
      outputStream.write(buffer, 0, size);
      writtenBytes += size;
      if (writtenBytes == fileSize) {
        outputStream.close();
        LOGGER.info("Saved {}. Written {} bytes of {}", fileName, writtenBytes, fileSize);
      }
    } catch (ClosedChannelException ignored) {
      // Пока игнорируем исключение
    }
  }

  public static List<FileInfo> updatePath(Path path) {
    ArrayList<FileInfo> result = new ArrayList<>();
    try {
      result.addAll(list(path).map(FileInfo::new)
                              .collect(Collectors.toList()));
    } catch (IOException e) {
      LOGGER.error(e.getLocalizedMessage());
    }
    return result;
  }

  public static void sendFile(ChannelHandlerContext ctx,
                              String fileName) throws IOException {
    final int BUFFER_SIZE = 128 * 1024; //размер буфера
    File file = new File(fileName);
    ctx.writeAndFlush(file.length()); // передаем размер файла
    try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
      byte[] tmpBuffer = new byte[BUFFER_SIZE]; // передаем файл блоками по BUFFER_SIZE
      int readed; // считаем, сколько передано
      while ((readed = fileInputStream.read(tmpBuffer)) > 0) {
        if (readed <
            BUFFER_SIZE) { // записываем последнюю часть файла, которая меньше нашего буфера
          byte[] smallBuffer = new byte[readed];
          System.arraycopy(tmpBuffer, 0, smallBuffer, 0, readed);
          ctx.writeAndFlush(smallBuffer);
        }
        ctx.writeAndFlush(tmpBuffer);
      }
      LOGGER.info("File {} seeded", fileName);
    }
  }
}
