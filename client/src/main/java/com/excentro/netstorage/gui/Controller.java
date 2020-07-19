package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.Commands;
import com.excentro.netstorage.commons.FileActions;
import com.excentro.netstorage.commons.SrcDst;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {
  static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);
  @FXML VBox localFiles;
  @FXML VBox remoteFiles;
  PanelController localPC;
  PanelController remotePC;
  FileClient client;
  Path srcPath;
  Path dstPath;

  public void cmdExit() {
    client.stop();
    Platform.exit();
  }

  public void copyBtnAction() throws IOException {

    if (localPC.getSelectedFileName() == null && remotePC.getSelectedFileName() == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "No files chosen", ButtonType.OK);
      alert.showAndWait();
    }

    PanelController srcPC;
    PanelController dstPC;

    // Заливаем на сервер
    if (localPC.getSelectedFileName() != null) {
      srcPC = localPC;
      dstPC = remotePC;
      srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());
      dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());
      LOGGER.info("Trying to send file from {} to {}", srcPath, dstPath);
      FileActions.sendFile(client.getFileHandler().context, srcPath, dstPath);
      client.sendCommand(Commands.DIR);
    }
    // скачиваем с сервера
    if (remotePC.getSelectedFileName() != null) {
      srcPC = remotePC;
      dstPC = localPC;
      srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());
      dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());
      SrcDst srcDst = new SrcDst(srcPath.toString(), dstPath.toString());
      client.getFileHandler().context.writeAndFlush(srcDst);
      client.sendCommand(Commands.DOWNLOAD);
      localPC.updateFiles(Paths.get(srcPC.getCurrentPath()));
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    localPC = (PanelController) localFiles.getProperties().get("ctrl");
    remotePC = (PanelController) remoteFiles.getProperties().get("ctrl");
    client = new FileClient("127.0.0.1", 8888, remotePC);
    Thread t = new Thread(client);
    t.setDaemon(true);
    t.start();
  }
}
