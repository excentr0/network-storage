package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.Commands;
import com.excentro.netstorage.commons.FileActions;
import com.excentro.netstorage.commons.FileInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class FileHandler extends ChannelInboundHandlerAdapter {
  static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);
  PanelController remotePanel;
  private long fileSize = 0;
  private ChannelHandlerContext context;

  public FileHandler(PanelController remotePanel) {
    this.remotePanel = remotePanel;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    //    ctx.writeAndFlush("d:\\tmp\\1.mp4");
    this.context = ctx;
    LOGGER.info("Established connection {}", context.channel());
    ctx.writeAndFlush(Commands.DIR);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    this.context = ctx;
    ByteBuf buf = ctx.alloc().buffer();
    try {
      if (msg instanceof String) {
        LOGGER.info(String.valueOf(msg));
        if (((String) msg).startsWith("Ready")) {
          FileActions.sendFile(ctx, "D:\\tmp\\1.mp4");
        }
      } else if (msg instanceof Long) {
        fileSize = (long) msg;
        LOGGER.info("File size is {}", fileSize);
      } else if (msg instanceof byte[]) {
        buf.writeBytes((byte[]) msg);
        FileActions.saveFile(buf, "D:\\tmp\\4.mp4", 627417589);
      } else if (msg instanceof ArrayList) {
        updateRemoteDir((ArrayList) msg);
        remotePanel.pathField = new TextField("."); // TODO передать удаленную папку
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      buf.release();
    }
  }

  private void updateRemoteDir(ArrayList msg) {
    ArrayList<FileInfo> files = msg;
    remotePanel.localFiles.getItems().clear();
    remotePanel.localFiles.getItems().addAll(files);
    remotePanel.localFiles.sort();
  }

  public void sendCommand(Commands command) {
    this.context.writeAndFlush(command);
  }
}
