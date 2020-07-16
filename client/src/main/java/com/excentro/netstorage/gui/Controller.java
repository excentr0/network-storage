package com.excentro.netstorage.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Controller implements Initializable {
  @FXML VBox localFiles;
  @FXML VBox remoteFiles;
  PanelController localPC;
  PanelController remotePC;
  private FileClient client;

  public void cmdExit() {
    Platform.exit();
  }

  public void copyBtnAction() {
    localPC = (PanelController) localFiles.getProperties().get("ctrl");
    remotePC = (PanelController) remoteFiles.getProperties().get("ctrl");

    if (localPC.getSelectedFileName() == null && remotePC.getSelectedFileName() == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "No files chosen", ButtonType.OK);
      alert.showAndWait();
    }

    PanelController srcPC = null;
    PanelController dstPC = null;

    if (localPC.getSelectedFileName() != null) {
      srcPC = localPC;
      dstPC = remotePC;
    }
    if (remotePC.getSelectedFileName() != null) {
      srcPC = remotePC;
      dstPC = localPC;
    }

    Path srcPath = Paths.get(srcPC.getCurrentPath(), srcPC.getSelectedFileName());
    Path dstPath = Paths.get(dstPC.getCurrentPath()).resolve(srcPath.getFileName().toString());

    try {
      Files.copy(srcPath, dstPath);
      dstPC.updateFiles(Paths.get(dstPC.getCurrentPath()));
    } catch (IOException e) {
      Alert alert = new Alert(Alert.AlertType.ERROR, "File already exists", ButtonType.OK);
      alert.showAndWait();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      client = new FileClient("127.0.0.1", 8888, localFiles, remoteFiles);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Path serverPath = Paths.get(".");
    remotePC.updateFiles(serverPath);
  }
}
