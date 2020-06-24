package com.excentro.netstorage.server.nioserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
  private static final Logger logger = LogManager.getLogger(NIOServer.class);

  public static void main(String[] args) throws IOException {
    Path path = Paths.get("./", "download", "file.txt");

    SocketChannel server;
    // запускаем сервер на порту 8888
    try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
      serverSocket.socket().bind(new InetSocketAddress(8888));
      // принимаем соединения
      server = serverSocket.accept();
    }
    logger.info("Got connection from: {}", server.getRemoteAddress());
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
