package com.excentro.netstorage.server.nioserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Запускает NIO сервер на порту 8888 и ждет подключения, после чего отправляет файл */
public class NIOServer {
  static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);

  public static void main(String[] args) throws IOException {
    Path path = Paths.get("./", "download", "file.txt");
    SocketChannel server;
    // запускаем сервер на порту 8888
    LOGGER.info("Starting server on port 8888");
    try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
      serverSocket.socket().bind(new InetSocketAddress(8888));
      // ждем соединения
      server = serverSocket.accept();
      LOGGER.info("Server started successfully");
    }
    LOGGER.info("Got connection from: {}", server.getRemoteAddress());
    // открываем файл
    try (FileChannel fileChannel = FileChannel.open(path)) {
      ByteBuffer buffer = ByteBuffer.allocate(256);
      // читаем файл в буфер
      while (fileChannel.read(buffer) > 0) {
        buffer.flip();
        // отправляем клиенту данные буфера
        server.write(buffer);
        buffer.clear();
      }
    }
    server.close();
  }
}
