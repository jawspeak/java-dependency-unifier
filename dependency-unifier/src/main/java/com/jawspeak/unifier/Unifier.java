package com.jawspeak.unifier;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;

public class Unifier {
  private ClassPathFactory classPathFactory = new ClassPathFactory();

  public static void main(String... args) throws IOException {
    Unifier unifier = new Unifier();
    if (unifier.validateArgs(System.err, args)) {
      unifier.unify(args[0], args[1], new File(args[2]));
    }
  }

  public void unify(String jarA, String jarB, File outputDir) throws IOException {
    ClassPath jarAClassPath = classPathFactory.createFromPath(jarA);
    ClassInfoMapBuilder builderA = new ClassInfoMapBuilder(jarAClassPath);
    Map<String, ClassInfo> jarAClasses = builderA.calculate();
    
    ClassPath jarBClassPath = classPathFactory.createFromPath(jarB);
    ClassInfoMapBuilder builderB = new ClassInfoMapBuilder(jarBClassPath);
    Map<String, ClassInfo> jarBClasses = builderB.calculate();
    
    ClassPathDiffer classPathDiffer = new ClassPathDiffer(jarAClasses, jarBClasses);
    
    new ClassGenerator(classPathDiffer.changesNeededInAToMatchB()).generate(outputDir.getCanonicalFile() + "/modifiedAToMatchB");
    new ClassGenerator(classPathDiffer.changesNeededInBToMatchA()).generate(outputDir.getCanonicalFile() + "/modifiedBToMatchA");
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
