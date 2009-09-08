package com.jawspeak.unifier;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.common.collect.ImmutableMap;

public class ClassInfoMapBuilderTest {

  private static final String TEST_RESOURCES = "src/test/resources/";
  private final ClassPathFactory classPathFactory = new ClassPathFactory();

  @Test
  public void readsEmptyJar() throws Exception {
    ClassPath singleClassInJarClassPath = classPathFactory.createFromPath(TEST_RESOURCES + "empty-jar.jar");
    Map<String, ClassInfo> map = new ClassInfoMapBuilder(singleClassInJarClassPath).calculate();
    assertEquals(new HashMap<String, ClassInfo>(), map);
  }
  
  @Test
  public void readsSingleEntryJar() throws Exception {
    ClassPath singleClassInJarClassPath = classPathFactory.createFromPath(TEST_RESOURCES + "single-class-in-jar.jar");
    Map<String, ClassInfo> map = new ClassInfoMapBuilder(singleClassInJarClassPath).calculate();
    String fqcn = "com.jawspeak.unifier.dummy.DoNothingClass1";
    Map<String, ClassInfo> expected = ImmutableMap.of(fqcn, new ClassInfo(fqcn));
    assertEquals(expected, map);
  }

  @Test
  public void readsInnerClassJar() throws Exception {
    ClassPath singleClassInJarClassPath = classPathFactory.createFromPath(TEST_RESOURCES + "class-and-inner-classes-in-jar.jar");
    Map<String, ClassInfo> map = new ClassInfoMapBuilder(singleClassInJarClassPath).calculate();
    String fqcn1 = "com.jawspeak.unifier.dummy.DoNothingClass1";
    String fqcn2 = "com.jawspeak.unifier.dummy.DoNothingClass1.InnerClass1";
    String fqcn3 = "com.jawspeak.unifier.dummy.DoNothingClass1.InnerClass1.InnerClass2";
    Map<String, ClassInfo> expected = ImmutableMap.of(fqcn1, new ClassInfo(fqcn1), fqcn2, new ClassInfo(fqcn2), fqcn3, new ClassInfo(fqcn3));
    assertEquals(expected, map);
  }
}
