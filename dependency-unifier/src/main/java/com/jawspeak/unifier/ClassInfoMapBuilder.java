package com.jawspeak.unifier;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.classpath.ClassPath;
import com.google.classpath.RegExpResourceFilter;
import com.google.classpath.ResourceFilter;

public class ClassInfoMapBuilder {

  private final ResourceFilter resourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
  private final ClassPath classPath;

  public ClassInfoMapBuilder(ClassPath classPath) {
    this.classPath = classPath;
  }

  public Map<String, ClassInfo> calculate() {
    SortedSet<String> classNames = classesInPath(classPath);
    Map<String, ClassInfo> map = new HashMap<String, ClassInfo>();
    for (String className : classNames) {
      String fqcn = formatAsFullyQualifiedClassName(className);
      ClassInfo classInfo = buildClassInfo(fqcn, classPath.getResourceAsStream(className));
      map.put(fqcn, classInfo);
    }
    return map;
  }
  
  private String formatAsFullyQualifiedClassName(String className) {
    return className.replace('/', '.').replaceAll("\\.class$", "").replace('$', '.');
  }

  private ClassInfo buildClassInfo(String fullyQualifiedClassName, InputStream resourceAsStream) {
    return new ClassInfo(fullyQualifiedClassName);
  }

  private SortedSet<String> classesInPath(ClassPath path) {
    SortedSet<String> jarAClasses = new TreeSet<String>();
    jarAClasses.addAll(Arrays.asList(path.findResources("", resourceFilter)));
    return jarAClasses;
  }


}
