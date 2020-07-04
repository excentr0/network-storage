package com.excentro.netstorage.nettyclient;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
  static final Logger       LOGGER       = LoggerFactory.getLogger(ClientHandler.class);
  private      OutputStream outputStream;
  private      int          writtenBytes = 0;
  private      byte[]       buffer       = new byte[0];


  @Override
  public void channelActive(ChannelHandlerContext ctx) throws IOException {
    LOGGER.info("Channel active: {}", ctx.channel());
    uploadFile(ctx, Paths.get("D:\\tmp\\2.mp4"));
  }

  /**
   * Calls {@link ChannelHandlerContext#fireChannelInactive()} to forward
   * to the next {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
   * <p>
   * Sub-classes may override this method to change behavior.
   *
   * @param ctx
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (this.outputStream != null) {
      this.outputStream.close();
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error("Error: {}", cause.getLocalizedMessage());
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
    saveFile(byteBuf);
  }

  private void saveFile(ByteBuf byteBuf) throws IOException {
    File file = new File("D:\\tmp\\3.mp4");
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


}
