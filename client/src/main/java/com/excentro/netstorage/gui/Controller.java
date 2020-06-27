package com.excentro.netstorage.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Controller {
  @FXML VBox localFiles;
  @FXML VBox remoteFiles;
  private Network network;

  public void cmdExit() {
    Platform.exit();
  }

  public void copyBtnAction() {
    PanelController localPC = (PanelController) localFiles.getProperties().get("ctrl");
    PanelController remotePC = (PanelController) remoteFiles.getProperties().get("ctrl");

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
}
