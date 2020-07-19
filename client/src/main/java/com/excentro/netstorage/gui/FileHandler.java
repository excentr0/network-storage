package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@ChannelHandler.Sharable
public class FileHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
  long fileSize = 0;
  ChannelHandlerContext context;
  private PanelController remotePanel;
  private Path dstPath;
  private Path srcPath;

  public FileHandler(PanelController remotePanel) {
    this.remotePanel = remotePanel;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    this.context = ctx;
    LOGGER.info("Established connection {}", context.channel());
    ctx.writeAndFlush(Commands.DIR);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    this.context = ctx;
    ByteBuf buf = ctx.alloc().buffer();
    LOGGER.info(String.valueOf(msg));
    try {
      if (msg instanceof String) {
        LOGGER.info(msg.toString());
      } else if (msg instanceof byte[]) { // принимаем файл
        buf.writeBytes((byte[]) msg);
        FileActions.saveFile(buf, fileSize, dstPath);
      } else if (msg instanceof Dir) { // принимаем содержимое папки
        Dir temDir = (Dir) msg;
        updateRemoteDir(temDir.getPath(), (ArrayList<FileInfo>) temDir.getFileInfos());
      } else if (msg instanceof FileInfo) { // принимаем информацию о файле
        fileSize = ((FileInfo) msg).getSize();
        LOGGER.info("Got FileInfo: {}", msg);
      } else if (msg instanceof SrcDst) { // принимаем отдкуда и куда копировать
        LOGGER.info("Got path {}", msg);
        dstPath = Paths.get(((SrcDst) msg).getDst());
        srcPath = Paths.get(((SrcDst) msg).getSrc());
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      buf.release();
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  private void updateRemoteDir(Path path, ArrayList<FileInfo> fileInfos) {
    remotePanel.pathField.setText(path.toString());
    remotePanel.localFiles.getItems().clear();
    remotePanel.localFiles.getItems().addAll(fileInfos);
    remotePanel.localFiles.sort();
  }

  public void sendCommand(Commands command) {
    this.context.writeAndFlush(command);
  }
}
