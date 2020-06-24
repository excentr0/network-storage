package com.excentro.netstorage.nioclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

/** Скачивает файл с сервера на НИО */
public class NIOClient {
  private static final Logger logger = LogManager.getLogger(NIOClient.class);

  public static void main(String[] args) throws IOException {
    String fileToWrite = "D:/tmp/file.txt";

    try (SocketChannel client = SocketChannel.open()) {
      // Открываем соединение на localhost порт 8888
      SocketAddress socketAddr = new InetSocketAddress(8888);
      client.connect(socketAddr);
      // Открываем файл на запись
      try (RandomAccessFile writer = new RandomAccessFile(fileToWrite, "rw")) {
        FileChannel fileChannel = writer.getChannel();
        // буфер чтения
        ByteBuffer buffer = ByteBuffer.allocate(256);
        // читаем в буфер данные
        while (client.read(buffer) > 0) {
          buffer.flip();
          // пишем в файл
          fileChannel.write(buffer);
          buffer.clear();
        }
      }
      logger.info("File Downloaded successfully");
    }
  }
}
