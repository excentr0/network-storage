package com.excentro.netstorage.gui;

import com.excentro.netstorage.commons.FileInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
  @FXML
  TextField           pathField; // кнопка диска
  @FXML
  ComboBox<String>    disksBox; // список дисков
  @FXML
  TableView<FileInfo> localFiles; // список фалов

  @Override
  public void initialize(final URL location,
                         final ResourceBundle resources) {
    TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
    fileTypeColumn.setCellValueFactory(
        param -> new SimpleStringProperty(param.getValue()
                                               .getType()
                                               .getName()));
    fileTypeColumn.setPrefWidth(24);

    TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
    fileNameColumn.setCellValueFactory(
        param -> new SimpleStringProperty(param.getValue()
                                               .getFilename()));
    fileNameColumn.setPrefWidth(240);

    TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
    fileSizeColumn.setCellValueFactory(
        param -> new SimpleObjectProperty<>(param.getValue()
                                                 .getSize()));
    fileSizeColumn.setPrefWidth(100);
    fileSizeColumn.setCellFactory(
        column ->
            new TableCell<FileInfo, Long>() {
              @Override
              protected void updateItem(final Long item,
                                        final boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                  setText(null);
                  setStyle("");
                } else {
                  String text = String.format("%,d bytes", item);
                  if (item == -1L) {
                    text = "[DIR]";
                  }
                  setText(text);
                }
              }
            });

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Updated");
    fileDateColumn.setCellValueFactory(
        param ->
            new SimpleStringProperty(param.getValue()
                                          .getLastModified()
                                          .format(timeFormatter)));
    fileDateColumn.setPrefWidth(120);

    localFiles.getColumns()
              .addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);

    // сортируем список файлов по типу файлов
    localFiles.getSortOrder()
              .add(fileTypeColumn);

    disksBox.getItems()
            .clear();
    for (Path p : FileSystems.getDefault()
                             .getRootDirectories()) {
      disksBox.getItems()
              .add(p.toString());
    }
    disksBox.getSelectionModel()
            .select(0);

    localFiles.setOnMouseClicked(
        event -> {
          if (event.getClickCount() == 2) {
            Path path =
                Paths.get(pathField.getText())
                     .resolve(localFiles.getSelectionModel()
                                        .getSelectedItem()
                                        .getFilename());
            if (Files.isDirectory(path)) {
              updateFiles(path);
            }
          }
        });

    updateFiles(Paths.get(".")); // читаем папку A
  }

  public void updateFiles(Path path) {
    // текущая папка
    pathField.setText(path.normalize()
                          .toAbsolutePath()
                          .toString());

    localFiles.getItems()
              .clear(); // чистим список файлов

    try {
      localFiles
          .getItems()
          .addAll(Files.list(path)
                       .map(FileInfo::new)
                       .collect(Collectors.toList()));
      localFiles.sort();
    } catch (IOException e) {
      // показываем Alert, если не удалось создать список файлов
      Alert alert = new Alert(Alert.AlertType.WARNING, "Can't update file list", ButtonType.OK);
      alert.showAndWait();
    }
  }

  public void btnPathUpAction() {
    Path upperPath = Paths.get(pathField.getText())
                          .getParent();
    if (upperPath != null) {
      updateFiles(upperPath);
    }
  }

  public void selectDiskAction(final ActionEvent actionEvent) {
    ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
    updateFiles(Paths.get(element.getSelectionModel()
                                 .getSelectedItem()));
  }

  public String getSelectedFileName() {
    if (!localFiles.isFocused()) {
      return null;
    }
    return localFiles.getSelectionModel()
                     .getSelectedItem()
                     .getFilename();
  }

  public String getCurrentPath() {
    return pathField.getText();
  }
}
