package com.jawspeak.unifier.spike;

import static com.google.classpath.RegExpResourceFilter.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
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
import org.objectweb.asm.Type;

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
  public void differenceBetweenInternalAndDescriptorName() throws Exception {
    String descriptor = Type.getDescriptor(String.class);
    String internalName = Type.getInternalName(String.class);
    assertEquals(descriptor, "L" + internalName + ";");
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
    private boolean isStatic; // TODO don't push this on local variables part, if it is static
    
    public ShimMethod(String name, String thisClassDesc, Class<?> returnClazz, Class<?>... parameters) {
      super();
      this.name = name;
      this.thisClassDesc = thisClassDesc;
      this.parameters = Lists.newArrayList(parameters);
      this.returnClazz = returnClazz;
    }
  }

  static class AddingClassAdapter extends ClassAdapter {
    private final ShimClass newClass;

    public AddingClassAdapter(ClassVisitor writer, ShimClass newClass) {
      super(writer);
      this.newClass = newClass;
    }
    // TODO: I think we just need to call lots of different visitors in here. VIsit field, visit method, visit etc. 
    @Override
    public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
      // TODO Auto-generated method stub
      super.visit(arg0, arg1, arg2, arg3, arg4, arg5);
    }
  }
  
  static class ModifyingClassAdapter extends ClassAdapter {
    private ShimMethod newMethod; // could be a list
    private ShimField newField;

    public ModifyingClassAdapter(ClassVisitor writer, ShimMethod newMethod, ShimField newField) {
      super(writer);
      this.newMethod = newMethod;
      this.newField = newField;
    }
    
    @Override
    public void visitEnd() {
      if (newField != null) {
        // Just this will add the field, but not set it to the default value. Thus it'll let compilation work, but may fail with runtime. (NPE)
        FieldVisitor fv = super.visitField(newField.access, newField.name, newField.desc, null, null);
        fv.visitEnd();
      }
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
      }
      
      super.visitEnd();
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
      
    ShimField newField = new ShimField("myString", Type.getDescriptor(String.class), Opcodes.ACC_PUBLIC); 
    
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
    assertNull(newClass.getField("myString").get(newClass.newInstance()));
  }
  
  static class ShimClass {
    private final String name;
    private final ShimField field;
    private final List<ShimMethod> methods;
    private final int version = Opcodes.V1_5;
    private final int access = Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER;
//    private final String signature = null; TODO later
    private final String superName;
    private final String[] interfaces;

    ShimClass(String name, List<ShimMethod> methods, ShimField field, String superName, String... interfaces) {
      this.name = name;
      this.methods = methods; // make plural
      this.field = field; // make plural
      this.superName = superName;
      this.interfaces = interfaces;
    }
  }

  @Test
  public void readsAsmAddsNewClass() throws Exception {
    String generatedBytecodeDir = GENERATED_BYTECODE + "/asm-adds-class/";

    ShimField newField = new ShimField("myString", Type.getDescriptor(String.class), Opcodes.ACC_PUBLIC); 
    ShimMethod newMethodInit = new ShimMethod("<init>", "Lcom/jawspeak/unifier/dummy/DoNothingClass1;", void.class); 
    ShimMethod newMethod1 = new ShimMethod("method1", "Lcom/jawspeak/unifier/dummy/DoNothingClass1;", void.class); 
    ShimClass shimClass = new ShimClass("com/jawspeak/unifier/dummy/DoNothingClass1", Lists.newArrayList(newMethodInit, newMethod1), newField, "java/lang/Object");
    
    
    writeOutAsmFilesWithNewClass(generatedBytecodeDir, shimClass);
    
    
    classPath = new ClassPathFactory().createFromPath(generatedBytecodeDir);
    final byte[] newBytes = readInputStream(classPath.getResourceAsStream("com/jawspeak/unifier/dummy/DoNothingClass1.class")).toByteArray();
    assertTrue(newBytes.length > 0);

    class MyClassLoader extends ClassLoader {
      Class<?> clazz;
    }
    MyClassLoader newClassLoader = new MyClassLoader() {{
      clazz = defineClass("com.jawspeak.unifier.dummy.DoNothingClass1", newBytes, 0, newBytes.length);
    }};
    
    Class<?> newClass = newClassLoader.clazz;
    assertEquals(1, newClass.getFields().length);
    Method[] methods = newClass.getMethods(); 
    Constructor[] constructors = newClass.getConstructors();
    try {
      newClass.newInstance();
      fail("expected exception");
    } catch (UnsupportedOperationException expected) {
      assertTrue(expected.getMessage().contains("Operation added to unify interfaces for compile time, but should not be called."));
    }
    assertEquals(2 + 8, newClass.getMethods().length);
    assertEquals(1, newClass.getConstructors().length);
    assertNotNull(newClass.getField("myString"));
    assertNotNull(newClass.getMethod("method1"));
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
      ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
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
      ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
      // The key: insert an adapter in here, as discussed in 2.2.4 of asm guide pdf.
      ModifyingClassAdapter adapter = new ModifyingClassAdapter(writer, newMethod, null);
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
      ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
      // The key: insert an adapter in here, as discussed in 2.2.4 of asm guide pdf.
      ModifyingClassAdapter adapter = new ModifyingClassAdapter(writer, null, newField);
      reader.accept(adapter, 0);
      FileOutputStream os = new FileOutputStream(new File(packageDir, pathAndFile[1]));
      os.write(writer.toByteArray());
      os.close();
    }
  }
  

  private void writeOutAsmFilesWithNewClass(String outputBaseDir, ShimClass newClass) throws IOException {
    File outputBase = new File(outputBaseDir);
    outputBase.mkdir();
    String[] pathAndFile = splitResourceToPathAndFile(newClass.name);
    File packageDir = new File(outputBase, pathAndFile[0]);
    packageDir.mkdirs();
    
    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    writer.visit(newClass.version, newClass.access, newClass.name, null, newClass.superName, newClass.interfaces);
    if (newClass.field != null) {
      ShimField newField = newClass.field;
      // Just this will add the field, but not set it to the default value. Thus it'll let compilation work, but may fail with runtime. (NPE)
      FieldVisitor fv = writer.visitField(newField.access, newField.name, newField.desc, null, null);
      fv.visitEnd();
    }
    
    for (ShimMethod newMethod : newClass.methods) {
    // could also have a List<Runnable> like Misko did and record what i want to do in here, then execute it at end.
      if (newMethod != null) {
        // TODO handle these params via helper classes / the ShimClass's methods.
        // I created this code with the ASMifier eclipse plugin tool.
        MethodVisitor mv = writer.visitMethod(Opcodes.ACC_PUBLIC, newMethod.name, "()V", null, null);
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
        mv.visitMaxs(3, 1);
        mv.visitEnd();
      }
    }
    writer.visitEnd();
    FileOutputStream os = new FileOutputStream(new File(packageDir, pathAndFile[1] + ".class"));
    os.write(writer.toByteArray());
    os.close();
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
    int lastSlash = resource.lastIndexOf("/");
    if (lastSlash == -1) {
      return new String[] {"", resource};
    }
    return new String[] {resource.substring(0, lastSlash), resource.substring(lastSlash, resource.length())};
  }
}
