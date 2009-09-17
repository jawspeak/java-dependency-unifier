package com.jawspeak.unifier.spike;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import com.google.common.collect.Lists;

public class ReadJarWriteJarTest {
  
  private ClassPath classPath;
  private static final String GENERATED_BYTECODE = "target/test-generated-bytecode";
  
  @BeforeClass
  public static void setUpOnce() {
    File toDelete = new File(GENERATED_BYTECODE);
//    System.out.println(toDelete + " exists? " + toDelete.exists());
//    stackDelete(toDelete);
    recursiveDelete(toDelete);
//    System.out.println(toDelete + " exists? " + toDelete.exists());
  }

  // this is a spike, so hey, I can implement this just for fun
  private static void stackDelete(File file) {  
    Stack<File> toDelete = new Stack<File>();
    toDelete.add(file);
    while (!toDelete.isEmpty()) {
      File f = toDelete.peek();
      if (f.isDirectory() && f.listFiles().length == 0) { // empty directory
        toDelete.pop().delete();
      } else if (f.isDirectory()) { // full directory
        for (File listing : f.listFiles()) {
          toDelete.push(listing);
        }
      } else { // file
        toDelete.pop().delete();
      }
    }
  }

  
  private static void recursiveDelete(File file) {
    if (file.isDirectory()) {
      for (File listing : file.listFiles()) {
        recursiveDelete(listing);
      }
      file.delete();
    } else {
      file.delete();
    }
  }

  @Test
  public void readsAndThenWritesJar() throws Exception {
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    String[] resources = classPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS));
    assertEquals("[com/jawspeak/unifier/dummy/DoNothingClass1.class]", Arrays.deepToString(resources));
    
    assertFalse("no dots in path", classPath.isPackage("."));
    assertFalse(classPath.isPackage("com.jawspeak.unifier.dummy"));
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
    
    String generatedBytecodeDir = GENERATED_BYTECODE + "/read-then-write-jar/";
    writeOutDirectFiles(generatedBytecodeDir, resources);
    
    classPath = new ClassPathFactory().createFromPath(generatedBytecodeDir);
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
  }

  @Test
  public void readsPassesThroughAsmThenWritesJar() throws Exception {
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    String[] resources = classPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS));
    assertEquals("[com/jawspeak/unifier/dummy/DoNothingClass1.class]", Arrays.deepToString(resources));
    
    assertFalse("no dots in path", classPath.isPackage("."));
    assertFalse(classPath.isPackage("com.jawspeak.unifier.dummy"));
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
    final byte[] originalClassBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    
    String generatedBytecodeDir = GENERATED_BYTECODE + "/read-then-asm-passthrough-write-jar/";
    writeOutAsmFiles(generatedBytecodeDir, resources);
    
    classPath = new ClassPathFactory().createFromPath(generatedBytecodeDir);
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
    final byte[] newBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    assertTrue(newBytes.length > 0);
    
    class MyClassLoader extends ClassLoader {
      Class<?> clazz;
    }
    MyClassLoader originalClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", originalClassBytes, 0, originalClassBytes.length);
    }};
    MyClassLoader newClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", newBytes, 0, newBytes.length);
    }};
    
    // load from both classloaders, and the methods should be the same. Could test more, but this
    // proves the spike of reading from asm and writing back out.
    Class<?> originalClass = originalClassLoader.clazz;
    Class<?> newClass = newClassLoader.clazz;
    Method[] originalMethods = originalClass.getMethods();
    Method[] newMethods = newClass.getMethods();
    assertEquals(originalMethods.length, newMethods.length);
    for (int i = 0; i < originalMethods.length; i++) { 
      assertEquals(originalMethods[i].toString(), newMethods[i].toString());
    }
    
    // Create a new jar from the newly asm-generated classes
    String command = "jar cf " + GENERATED_BYTECODE + "/single-class-in-jar-asm-modified.jar -C " + generatedBytecodeDir + " .";
    Process process = Runtime.getRuntime().exec(command);
    process.waitFor();
    String stdout = new String(readInputStream(process.getInputStream()).toByteArray());
    String stderr = new String(readInputStream(process.getErrorStream()).toByteArray());
    assertEquals("", stdout);
    assertEquals("", stderr);
    assertTrue(new File(GENERATED_BYTECODE + "/single-class-in-jar-asm-modified.jar").exists());
    
    // Then we should be able to read back out the new jar, with all the same resources.
    classPath = new ClassPathFactory().createFromPath(GENERATED_BYTECODE + "/single-class-in-jar-asm-modified.jar");
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
  }
  
  static class ShimMethod {
    private String name;
    private List<Class<?>> parameters;
    private Class<?> returnClazz;
    private final String thisClassDesc;
    
    public ShimMethod(String name, String thisClassDesc, Class<?> returnClazz, Class<?>... parameters) {
      super();
      this.name = name;
      this.thisClassDesc = thisClassDesc;
      this.parameters = Lists.newArrayList(parameters);
      this.returnClazz = returnClazz;
    }
    public String myString = "some value";
  }

  static class AddingClassAdapter extends ClassAdapter {
    private ShimMethod newMethod; // could be a list
    private ShimField newField;

    public AddingClassAdapter(ClassVisitor writer, ShimMethod newMethod, ShimField newField) {
      super(writer);
      this.newMethod = newMethod;
      this.newField = newField;
    }
    
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      if (newField != null) {
        // Just this will add the field, but not set it to the default value. Thus it'll let compilation work, but may fail with runtime. (NPE)
        FieldVisitor fv = super.visitField(Opcodes.ACC_PUBLIC, newField.name, newField.desc, null, null);
        fv.visitEnd();
        newField = null;
      }
      return super.visitField(access, name, desc, signature, value);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      // could also have a List<Runnable> like Misko did and record what i want to do in here, then execute it at end.
      if (newMethod != null) {
        // TODO handle these params via helper classes / the ShimClass's methods.
        // I created this code with the ASMifier eclipse plugin tool.
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, newMethod.name, "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/UnsupportedOperationException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn("Operation added to unify interfaces for compile time, but should not be called.");
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(Opcodes.ATHROW);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", newMethod.thisClassDesc, null, l0, l1, 0);
        mv.visitEnd();
        mv.visitMaxs(3, 1);
        newMethod = null;
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }
  
  @Test
  public void readsAsmAddsMethod() throws Exception {
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    String[] resources = classPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS));
    final byte[] originalClassBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    String generatedBytecodeDir = GENERATED_BYTECODE + "/read-then-asm-adds-method/";
      
    ShimMethod newMethod = new ShimMethod("myNewMethod", "Lcom/jawspeak/unifier/dummy/DoNothingClass1;", void.class); 
    
    writeOutAsmFilesWithNewMethod(generatedBytecodeDir, resources, newMethod);
    
    classPath = new ClassPathFactory().createFromPath(generatedBytecodeDir);
    final byte[] newBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    assertTrue(newBytes.length > 0);
    
    class MyClassLoader extends ClassLoader {
      Class<?> clazz;
    }
    MyClassLoader originalClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", originalClassBytes, 0, originalClassBytes.length);
    }};
    MyClassLoader newClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", newBytes, 0, newBytes.length);
    }};
    
    Class<?> originalClass = originalClassLoader.clazz;
    Class<?> newClass = newClassLoader.clazz;
    Method[] originalMethods = originalClass.getMethods();
    Method[] newMethods = newClass.getMethods();
    assertEquals(10, originalMethods.length);
    assertEquals(11, newMethods.length);
    newClass.getMethod("method1", String.class).invoke(newClass.newInstance(), ""); // should not have any exceptions
    try {
      newClass.getMethod("myNewMethod").invoke(newClass.newInstance());
      fail("Expected Exception: Should have thrown exception in new method we just generated");
    } catch (InvocationTargetException expected) { 
      UnsupportedOperationException cause = (UnsupportedOperationException) expected.getCause();
      assertEquals("Operation added to unify interfaces for compile time, but should not be called.", cause.getMessage());
    } 
  }

  class ShimField {
    String name;
    String desc;
    int access;
    public ShimField(String fieldName, String desc, int access) {
      this.name = fieldName;
      this.desc = desc;
      this.access = access;
    }
  }
  
  @Test
  public void readsAsmAddsField() throws Exception {
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    String[] resources = classPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS));
    final byte[] originalClassBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    String generatedBytecodeDir = GENERATED_BYTECODE + "/read-then-asm-adds-field/";
      
    ShimField newField = new ShimField("myString", "Ljava/lang/String;", 3); 
    
    writeOutAsmFilesWithNewField(generatedBytecodeDir, resources, newField);
    
    classPath = new ClassPathFactory().createFromPath(generatedBytecodeDir);
    final byte[] newBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    assertTrue(newBytes.length > 0);
    
    class MyClassLoader extends ClassLoader {
      Class<?> clazz;
    }
    MyClassLoader originalClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", originalClassBytes, 0, originalClassBytes.length);
    }};
    MyClassLoader newClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", newBytes, 0, newBytes.length);
    }};
    
    Class<?> originalClass = originalClassLoader.clazz;
    Class<?> newClass = newClassLoader.clazz;
    Field[] originalFields = originalClass.getFields();
    Field[] newFields = newClass.getFields();
    assertEquals(1, originalFields.length);
    assertEquals(2, newFields.length);
    assertNotNull(newClass.getField("myString"));
  }
  
  
  private void writeOutDirectFiles(String outputDir, String[] resources) throws IOException {
    File outputBase = new File(outputDir);
    outputBase.mkdir();
    for (String resource : resources) {
      String[] pathAndFile = splitResourceToPathAndFile(resource);
      File output = new File(outputBase, pathAndFile[0]);
      output.mkdirs();
      InputStream is = classPath.getResourceAsStream(resource);
      ByteArrayOutputStream baos = readInputStream(is);
      FileOutputStream os = new FileOutputStream(new File(output, pathAndFile[1]));
      os.write(baos.toByteArray());
      os.close();
    }
  }

  private void writeOutAsmFiles(String outputBaseDir, String[] resources) throws IOException {
    File outputBase = new File(outputBaseDir);
    outputBase.mkdir();
    for (String resource : resources) {
      String[] pathAndFile = splitResourceToPathAndFile(resource);
      File packageDir = new File(outputBase, pathAndFile[0]);
      packageDir.mkdirs();
      InputStream is = classPath.getResourceAsStream(resource);
      
      // Here's the key: read from the old bytes and write to the new ones. 
      // Ends up just copying the byte array, but next we'll look at inserting something 
      // interesting in between them.
      ClassReader reader = new ClassReader(is);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      reader.accept(writer, 0);
      FileOutputStream os = new FileOutputStream(new File(packageDir, pathAndFile[1]));
      os.write(writer.toByteArray());
      os.close();
    }
  }
  
  private void writeOutAsmFilesWithNewMethod(String outputBaseDir, String[] resources,
      ShimMethod newMethod) throws IOException {
    File outputBase = new File(outputBaseDir);
    outputBase.mkdir();
    for (String resource : resources) {
      String[] pathAndFile = splitResourceToPathAndFile(resource);
      File packageDir = new File(outputBase, pathAndFile[0]);
      packageDir.mkdirs();
      InputStream is = classPath.getResourceAsStream(resource);
      
      ClassReader reader = new ClassReader(is);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      // The key: insert an adapter in here, as discussed in 2.2.4 of asm guide pdf.
      AddingClassAdapter adapter = new AddingClassAdapter(writer, newMethod, null);
      reader.accept(adapter, 0);
      FileOutputStream os = new FileOutputStream(new File(packageDir, pathAndFile[1]));
      os.write(writer.toByteArray());
      os.close();
    }
  }
  
  private void writeOutAsmFilesWithNewField(String outputBaseDir, String[] resources,
      ShimField newField) throws IOException {
    File outputBase = new File(outputBaseDir);
    outputBase.mkdir();
    for (String resource : resources) {
      String[] pathAndFile = splitResourceToPathAndFile(resource);
      File packageDir = new File(outputBase, pathAndFile[0]);
      packageDir.mkdirs();
      InputStream is = classPath.getResourceAsStream(resource);
      
      ClassReader reader = new ClassReader(is);
      ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
      // The key: insert an adapter in here, as discussed in 2.2.4 of asm guide pdf.
      AddingClassAdapter adapter = new AddingClassAdapter(writer, null, newField);
      reader.accept(adapter, 0);
      FileOutputStream os = new FileOutputStream(new File(packageDir, pathAndFile[1]));
      os.write(writer.toByteArray());
      os.close();
    }
  }
  
  private ByteArrayOutputStream readInputStream(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int toRead;
    int offset = 0;
    while((toRead = is.available()) > 0) {
      byte[] buf = new byte[toRead];
      is.read(buf, offset, toRead);
      baos.write(buf);
      offset += toRead;
    }
    is.close();
    return baos;
  }

  private String[] splitResourceToPathAndFile(String resource) {
    int i = resource.length() - 1;
    while (i >= 0) {
      if (resource.charAt(i) == '/') {
        return new String[] {resource.substring(0, i), resource.substring(i + 1)};
      }
      i--;
    }
    return new String[] {"", resource};
  }
}
