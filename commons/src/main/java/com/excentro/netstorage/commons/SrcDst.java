package com.excentro.netstorage.commons;

import java.io.Serializable;

public class SrcDst implements Serializable {
  private String src, dst;

  public SrcDst(String src, String dst) {
    this.src = src;
    this.dst = dst;
  }

  public String getSrc() {
    return src;
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("SrcDst{");
    sb.append("src='").append(src).append('\'');
    sb.append(", dst='").append(dst).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public String getDst() {
    return dst;
  }
}
