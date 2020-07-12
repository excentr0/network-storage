package com.excentro.netstorage.commons;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class FileInfo implements Serializable {
  private final String        filename;
  private final FileType      type;
  private final LocalDateTime lastModified;
  private       long          size;

  public FileInfo(Path path) {
    try {
      this.filename = path.getFileName()
                          .toString();
      this.size     = Files.size(path);
      this.type     = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
      if (this.type == FileType.DIRECTORY) {
        this.size = -1L;
      }
      this.lastModified =
          LocalDateTime.ofInstant(
              Files.getLastModifiedTime(path)
                   .toInstant(), ZoneOffset.ofHours(3));
    } catch (IOException e) {
      throw new RuntimeException("Unable to create file info from path");
    }
  }

  public String getFilename() {
    return filename;
  }

  public FileType getType() {
    return type;
  }

  public long getSize() {
    return size;
  }

  public LocalDateTime getLastModified() {
    return lastModified;
  }

  @Override
  public String toString() {
    return "FileInfo{" +
           "filename='" + filename + '\'' +
           ", type=" + type +
           ", lastModified=" + lastModified +
           ", size=" + size +
           '}';
  }

  public enum FileType {
    FILE("F"),
    DIRECTORY("D");
    private String name;

    FileType(final String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
