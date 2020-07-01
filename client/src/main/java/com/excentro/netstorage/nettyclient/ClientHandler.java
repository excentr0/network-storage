package com.excentro.netstorage.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.stream.ChunkedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ClientHandler extends SimpleChannelInboundHandler<ChunkedFile> {
  static final Logger LOGGER = LoggerFactory.getLogger(ClientHandler.class);
  ObjectOutputStream outputStream = null;
  File outFile = new File("D:\\tmp\\newfile.txt");

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    // Send the first message if this handler is a client-side handler.
    ctx.writeAndFlush("file");
    LOGGER.info("{} channelActive called", ctx.channel());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    LOGGER.info("{} read complete", ctx.channel());
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());
    ctx.close();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ChunkedFile msg) throws Exception {
    ByteBuf in = msg.readChunk(ByteBufAllocator.DEFAULT);
    ByteBuffer byteBuffer = in.nioBuffer();
    try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
      FileChannel fileChannel = raf.getChannel();
      while (byteBuffer.hasRemaining()) {
        //        fileChannel.position(file.length());
        fileChannel.write(byteBuffer);
      }
      in.release();
      fileChannel.close();
    }
  }
}
