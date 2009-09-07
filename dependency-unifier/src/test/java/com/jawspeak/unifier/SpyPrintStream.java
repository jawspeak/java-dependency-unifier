package com.jawspeak.unifier;

import java.io.OutputStream;

public class SpyPrintStream extends OutputStream {
  StringBuffer sb = new StringBuffer(5000);

  @Override
  public void write(int ch) {
    sb.append(ch);
  }

  @Override
  public void write(byte[] b) {
    sb.append(new String(b));
  }

  @Override
  public void write(byte[] b, int off, int len) {
    sb.append(new String(b, off, len));
  }

  @Override
  public String toString() {
    return sb.toString();
  }

  public void clear() {
    sb = new StringBuffer(5000);
  }
}