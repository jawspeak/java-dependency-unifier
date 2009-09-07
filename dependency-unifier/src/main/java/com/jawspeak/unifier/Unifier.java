package com.jawspeak.unifier;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class Unifier {

  public static void main(String... args) {
    Unifier unifier = new Unifier();
    if (unifier.validateArgs(System.err, args)) {
      unifier.unify(new File(args[0]), new File(args[1]), new File(args[2]));
    }
  }

  public void unify(File jarA, File jarB, File outputDir) {

  }

  boolean validateArgs(OutputStream outputStream, String... args) {
    if (args.length != 3) {
      try {
        String error = "Usage: java " + Unifier.class.getName()
            + " jarA.jar jarB.jar unified-output-dir/\n";
        outputStream.write(error.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return false;
    }
    return true;
  }
}
