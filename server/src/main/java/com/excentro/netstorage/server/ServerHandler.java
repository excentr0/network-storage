package com.excentro.netstorage.server;

import com.excentro.netstorage.common.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.list;

public class ServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
  static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);


  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    LOGGER.info("{} channel active", ctx.channel());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx,
                              Throwable cause) {
    LOGGER.error(cause.getLocalizedMessage());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx,
                              ByteBuf msg) throws Exception {
    while (msg.isReadable()) {
      LOGGER.info("GOT {}", msg.readInt());
    }
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
