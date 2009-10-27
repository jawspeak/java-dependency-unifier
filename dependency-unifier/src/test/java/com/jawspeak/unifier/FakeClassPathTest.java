package com.jawspeak.unifier;

import static org.junit.Assert.*;
import static org.objectweb.asm.Opcodes.*;

import java.util.ArrayList;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;


public class FakeClassPathTest {

  @Test
  public void canonicalName() throws Exception {
    assertEquals("com/jawspeak/unifier/FakeClassPathTest$MyClass.class",
        MyClass.class.getName().replace('.', '/') + ".class");
  }
  
  @Test
  public void empty() throws Exception {
    FakeClassPath fakeClassPath = new FakeClassPath(new ArrayList<Class<?>>());
    assertEquals(0, fakeClassPath.findResources("", null).length);

    fakeClassPath = new FakeClassPath();
    assertEquals(0, fakeClassPath.findResources("", null).length);
  }
  
  private static class MyClass {
  }
  
  @Test
  public void oneClass() throws Exception {
    ArrayList<Class<?>> list = new ArrayList<Class<?>>();
    list.add(MyClass.class);
    FakeClassPath fakeClassPath = new FakeClassPath(list);
    assertEquals(1, fakeClassPath.findResources("", null).length);
    assertEquals(MyClass.class.getName().replace('.', '/') + ".class", fakeClassPath.findResources("", null)[0]);
    assertEquals(1, fakeClassPath.listResources(null).length);
    assertEquals(MyClass.class.getName().replace('.', '/') + ".class", fakeClassPath.listResources(null)[0]);
    assertTrue(fakeClassPath.isResource(null));
    assertFalse(fakeClassPath.isPackage(null));
    assertArrayEquals(fakeClassPath.listResources(null), fakeClassPath.findResources("", null));
    assertArrayEquals(new String[] {"com/jawspeak/unifier"}, fakeClassPath.listPackages(null));
  }
  
  @Test
  public void oneClassNotInAPackage() throws Exception {
    Class<?> clazz = makeClassWithoutAPackage();
    FakeClassPath fakeClassPath = new FakeClassPath(clazz);
    
    assertArrayEquals("Should not have any packages since class was created without a package", new String[] {}, fakeClassPath.listPackages(null));
  }
  
  @Test
  public void mixedClassInAndOutOfPackage() throws Exception {
    Class<?> clazz = makeClassWithoutAPackage();
    ArrayList<Class<?>> list = new ArrayList<Class<?>>();
    list.add(clazz);
    list.add(MyClass.class);
    FakeClassPath fakeClassPath = new FakeClassPath(list);
    
    assertArrayEquals(new String[] {"com/jawspeak/unifier"}, fakeClassPath.listPackages(null));
  }

  private Class<?> makeClassWithoutAPackage() {
    // Use ASM since we can't use mockito on Class to make it return a package-less class, Class is a final class.
    ClassWriter writer = new ClassWriter(0);
    writer.visit(V1_5, ACC_PUBLIC, "ClazzWithNoPackage", null, "java/lang/Object", null);
    writer.visitEnd();
    byte[] bytes = writer.toByteArray();
    class ClazzLoader extends ClassLoader {
      Class<?> defineClass(byte[] bytes) {
        return defineClass(null, bytes, 0, bytes.length);
      }
    };
    Class<?> clazz = new ClazzLoader().defineClass(bytes);
    assertEquals("ClazzWithNoPackage", clazz.getName());
    return clazz;
  }

}
