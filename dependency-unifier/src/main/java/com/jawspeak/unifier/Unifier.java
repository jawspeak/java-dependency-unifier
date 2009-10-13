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
     *  - creating a class that is completely missing
     *  - modifying an existing class that exists as an input stream in the ClassPath.
     * 
     * So, there are two algorithm choices:
     *  1) open up the jar and hold it in memory. programmatically create and modify classes in there.
     *  2) open up the jar and hold it in memory, calculate diffs, visit all classes and modify them, then write them out to disk, and jar it up.
     *  
     * My hunch is #2 is the proper approach. But that's because I don't know how to do 1. 
     * 
     * To prove this out, I need a spike: spike is to read a jar, and then build a jar. Same jar.
     * [Done 9-15] Then read a jar, and build a jar, same jar, but pass it through asm's tool to visit everything and copy it. No changes made.
     * [Done 9-16] Then same as above, but add methods, processing bytes and not necessarily writing files/jar.
     * [Done 9-16] Then same but add fields.
     * [Done 10-12] Then same but add whole classes.
     * Then build a diff between the two classes. Show how they differ.
     * Next up: a differ to diff between the files, and construct a changeset of the files that need to be altered. 
     *    This will represent method/field/constructor/class additions from one to another classpath root. 
     * Put the pieces together with Diffing two jars, and creating a combined common api.
     * Then take principles I learned in the spike and build something more robust and test driven.
     * Then write out new jar file.
     * 
     * Future Feature Roadmap:
     * Then keep using same (previous) manifests
     * Then keep using original jar's resources
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
