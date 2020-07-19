package com.excentro.netstorage.commons;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Dir implements Serializable {
  private final String path;
  private final List<FileInfo> fileInfos;

  public Dir(String path, List<FileInfo> fileInfos) {
    this.path = path;
    this.fileInfos = fileInfos;
  }

  public Path getPath() {
    return Paths.get(path);
  }

  public List<FileInfo> getFileInfos() {
    return fileInfos;
  }

  @Override
  public String toString() {
    return "Dir{" + "path=" + path + ", fileInfos=" + fileInfos + '}';
  }
}
