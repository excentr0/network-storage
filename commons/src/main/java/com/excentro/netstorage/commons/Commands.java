package com.excentro.netstorage.commons;

public enum Commands {
  UPLOAD(1), DOWNLOAD(2), MOVE(3), DELETE(4), DIR(5);
  private int code;

  Commands(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
