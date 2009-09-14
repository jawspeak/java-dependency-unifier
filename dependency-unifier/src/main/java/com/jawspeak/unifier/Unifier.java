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

    /* I'm looking at programmatically:
     *  - creating a class that is completly missing
     *  - modifying an existing class that exists as an input stream in the ClassPath.
     * 
     * So, there are two algorithm choices:
     *  1) open up the jar and hold it in memory. programmatically create and modify classes in there.
     *  2) open up the jar and hold it in memory, calculate diffs, visit all classes and modify them, then write them out to disk, and jar it up.
     *  
     * My hunch is #2 is the proper approach. But that's because I don't know how to do 1. 
     * 
     * To prove this out, I need a spike: spike is to read a jar, and then build a jar. Same jar.
     * Then read a jar, and build a jar, same jar, but pass it through asm's tool to visit everything and copy it. No changes made.
     * Then same as above, but add methods, writing it out to file.
     * Then same but add fields.
     * Then same but add whole classes.
     * Then, I've proven it out, and can check in.
     */
    
//    new ClassGenerator(classPathDiffer.changesNeededInAToMatchB()).generate();
//    new ClassOutputWriteroutputDir.getCanonicalFile() + "/modifiedAToMatchB");
//    new ClassGenerator(classPathDiffer.changesNeededInBToMatchA()).generate();
//    outputDir.getCanonicalFile() + "/modifiedBToMatchA");
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
