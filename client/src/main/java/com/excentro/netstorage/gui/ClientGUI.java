package com.excentro.netstorage.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientGUI extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/main_screen.fxml"));
    primaryStage.setTitle("Network Storage Client");
    primaryStage.setScene(new Scene(root, 1200, 600));
    primaryStage.show();
  }
}
