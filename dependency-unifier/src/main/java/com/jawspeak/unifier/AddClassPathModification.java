package com.jawspeak.unifier;

public class AddClassPathModification implements ClassPathModification {

  private final ClassInfo classInfoToAdd;

  public AddClassPathModification(ClassInfo classInfo) {
    this.classInfoToAdd = classInfo;
  }

}
