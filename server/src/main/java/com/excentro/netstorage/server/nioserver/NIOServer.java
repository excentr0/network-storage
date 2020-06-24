package com.excentro.netstorage.server.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServer implements Runnable {
  private static int clientCount = 0;
  private final ByteBuffer buffer = ByteBuffer.allocate(256);
  private final ServerSocketChannel serverSocketChannel;
  private final Selector selector;

  public NIOServer() throws IOException {
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.socket().bind(new InetSocketAddress(8189));
    serverSocketChannel.configureBlocking(false);
    selector = Selector.open();
    serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
  }

  public static void main(String[] args) throws IOException {
    new Thread(new NIOServer()).start();
  }

  public void run() {
    try {
      System.out.println("Server started on port 8189");
      Iterator<SelectionKey> iterator;
      SelectionKey key;
      while (serverSocketChannel.isOpen()) {
        int eventsCount = selector.select();
        System.out.println("Selected " + eventsCount + " events.");
        iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          key = iterator.next();
          iterator.remove();
          if (key.isAcceptable()) {
            handleAccess(key);
          }
          if (key.isReadable()) {
            handleRead(key);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void handleAccess(final SelectionKey key) throws IOException {
    SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
    clientCount++;
    String userName = "user#" + clientCount;
    channel.configureBlocking(false);
    channel.register(selector, SelectionKey.OP_READ, userName);
    channel.write(ByteBuffer.wrap(("Hello " + userName + "!").getBytes()));
    System.out.println("Client " + userName + " connected from ip: " + channel.getLocalAddress());
  }

  private void handleRead(final SelectionKey key) throws IOException {
    SocketChannel channel = (SocketChannel) key.channel();
    StringBuilder message = new StringBuilder();
    int read = 0;
    buffer.rewind();
    while ((read = channel.read(buffer)) > 0) {
      buffer.flip();
      byte[] bytes = new byte[buffer.limit()];
      buffer.get(bytes);
      message.append(new String(bytes));
      buffer.rewind();
    }
    if (read < 0) {
      System.out.println(key.attachment() + ": leave!");
      for (SelectionKey send : key.selector().keys()) {
        if (send.channel() instanceof SocketChannel && send.isReadable()) {
          ((SocketChannel) send.channel())
              .write(ByteBuffer.wrap((key.attachment() + ": leave!").getBytes()));
        }
      }
      channel.close();
    } else {
      System.out.println(key.attachment() + ": " + message);
      String msg = key.attachment() + ": " + message;
      for (SelectionKey send : key.selector().keys()) {
        if (send.channel() instanceof SocketChannel && send.isReadable()) {
          ((SocketChannel) send.channel()).write(ByteBuffer.wrap(msg.getBytes()));
        }
      }
    }
  }
}
