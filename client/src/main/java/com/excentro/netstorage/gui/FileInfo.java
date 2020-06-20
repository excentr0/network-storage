package com.excentro.netstorage.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo {
  private String        filename;
  private FileType      type;
  private long          size;
  private LocalDateTime lastModified;

  public FileInfo(Path path) {
    try {
      this.filename = path.getFileName()
                          .toString();
      this.size     = Files.size(path);
      this.type     = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
      if (this.type == FileType.DIRECTORY) {
        this.size = -1L;
      }
      this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path)
                                                       .toInstant(), ZoneOffset.ofHours(3));
    } catch (IOException e) {
      throw new RuntimeException("Unable to create file info from path");
    }

  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(final String filename) {
    this.filename = filename;
  }

  public FileType getType() {
    return type;
  }

  public void setType(final FileType type) {
    this.type = type;
  }

  public long getSize() {
    return size;
  }

  public void setSize(final long size) {
    this.size = size;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  public void setLastModified(final LocalDateTime lastModified) {
    this.lastModified = lastModified;
  }

  public enum FileType {
    FILE("F"), DIRECTORY("D");
    private String name;

    FileType(final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    private void setName(final String name) {
      this.name = name;
    }
  }

}
