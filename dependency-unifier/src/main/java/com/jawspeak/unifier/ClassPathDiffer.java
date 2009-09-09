package com.jawspeak.unifier;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class ClassPathDiffer {


  private final Map<String, ClassInfo> jarAClasses;
  private final Map<String, ClassInfo> jarBClasses;

  public ClassPathDiffer(Map<String, ClassInfo> jarAClasses, Map<String, ClassInfo> jarBClasses) {
    this.jarAClasses = jarAClasses;
    this.jarBClasses = jarBClasses;
  }

  public List<ClassPathModification> changesNeededInBToMatchA() {
    List<ClassPathModification> mods = modificationsBetween(jarAClasses, jarBClasses);
    return mods;
  }

  public List<ClassPathModification> changesNeededInAToMatchB() {
    List<ClassPathModification> mods = modificationsBetween(jarBClasses, jarAClasses);
    return mods;
  }
  
  private List<ClassPathModification> modificationsBetween(Map<String, ClassInfo> desiredClasses, Map<String, ClassInfo> conformTheseToMatch) {
    List<ClassPathModification> mods = Lists.newArrayList();
    for (Map.Entry<String, ClassInfo> entryA : desiredClasses.entrySet()) {
      String fqcnA = entryA.getKey();
      if (!conformTheseToMatch.containsKey(fqcnA)) {
        mods.add(new AddClassPathModification(entryA.getValue()));
      }
    }
    return mods;
  }
  
}
