package com.jawspeak.unifier;

import java.util.List;

public class ClassGenerator {

  private final List<ClassPathModification> classModifications;

  public ClassGenerator(List<ClassPathModification> classModifications) {
    this.classModifications = classModifications;
  }

  public void generate(String outputDir) {
    // TODO how do i instead want to pass around generated classes? I don't want this, because it requires an output directory to test. byte[] or custom classloader?
    
  }
}
