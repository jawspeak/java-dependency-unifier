package com.jawspeak.unifier;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.asm.Opcodes.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.asm.Opcodes;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import com.google.common.collect.Lists;
import com.jawspeak.unifier.Differ;
import com.jawspeak.unifier.changes.Change;
import com.jawspeak.unifier.changes.NewClass;
import com.jawspeak.unifier.changes.NewField;
import com.jawspeak.unifier.changes.NewMethod;

/**
 * I have a few implementation choices: 
 *   (1) Use java reflection to walk the API and build a representation of the classes.
 *     This would be DOM/Tree based.
 *   (2) Use ASM to visit all methods and accumulate information about the classes. This 
 *     Would be event based.
 * Basically, it's a decision of using SAX vs. DOM api's. 
 * 
 * (1) If using DOM, algo would look like:
 * 
 * Create a Map<Class or fully qualified classname String, ClassInfo>
 * For every class in classpath1:
 *   Record every Class, as a ClassInfo object.
 *   Record every Field. (Type, visibility, name, generics (? if supported later), etc -- what asm wants). 
 *   Record every method.
 *   Record every constructor.
 *   Record every interface it implements.
 * Repeat in classpath2.
 *   (see above)
 * For all of these Classes, translate into ASM friendly objects, or have my Builders translate
 * into the ASM types.
 * 
 * (Assuming we want classapth2 to look LIKE classpath1:
 *  For every class in classpath1:
 *      If the class A exists in classpath2?
 *        For every field/method in class A add if missing on class A' in classpath2
 *      Else:
 *        Re-implement class A with UOE's for the methods, and nulls for fields.
 *          
 *          
 * (2)  The event based model would be similar:
 * View every resource, and record their attributes in an ASM style way. 
 * Then, do the same diffing intermediate step. 
 * This sounds the simplest, as it will involve the least amount of translation/transformation
 * between differnt API/Domains (I just use ASM's types everywhere).
 */
public class DiffingTest {
  
  public static class ClassA {
    public String fieldA;
  }
  
  @Test
  public void resourceNames() throws Exception {
    String classAResource = ClassA.class.getName().replace('.', '/') + ".class";
    assertEquals("com/jawspeak/unifier/DiffingTest$ClassA.class",  classAResource); 
  }
  
  @Test
  public void sameClassIsNotDifferent() throws Exception {
    final ClassPath goldenClassPath = new FakeClassPath(ClassA.class);
    final ClassPath classPath2 = new FakeClassPath(ClassA.class);
    Differ differ = new Differ(goldenClassPath, classPath2);
    assertEquals(Lists.newArrayList(), differ.changesetToUnify());
  }
    
  public static class ClassB {
    public String fieldB;
  }
  
  @Test
  public void differentClassesAreDifferent() throws Exception {
    ClassPath goldenClassPath = new FakeClassPath(ClassA.class);
    ClassPath classPath2 = new FakeClassPath(ClassB.class);
    Differ differ = new Differ(goldenClassPath, classPath2);
    assertEquals(1, differ.changesetToUnify().size());
    
    NewMethod newMethod = new NewMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    NewField newField = new NewField(ACC_PUBLIC, "fieldB", "Ljava/lang/String;", null, null);
    // uses V1.5 because assumes the javac compiler is for java 1.5
    NewClass newClass = new NewClass(V1_5, ACC_PUBLIC + ACC_SUPER, "com/jawspeak/unifier/DiffingTest$ClassB", null, "java/lang/Object", null, Lists.newArrayList(newMethod), Lists.newArrayList(newField));
    assertEquals(Lists.newArrayList(newClass), differ.changesetToUnify());
  }
  
  
  @Test
  public void sameClassnameDifferentFields() throws Exception {
    RegExpResourceFilter fieldDifferenceFilter = new RegExpResourceFilter(RegExpResourceFilter.ANY, ".*FieldDifference\\.class");
    ClassPath goldenClassPath = new FilteringClassPath(
        new ClassPathFactory().createFromPath("../dependency-unifier-testdependency-a/target/classes"), fieldDifferenceFilter);
    ClassPath classPath2 = new FilteringClassPath(
        new ClassPathFactory().createFromPath("../dependency-unifier-testdependency-b/target/classes"), fieldDifferenceFilter);
    
    Differ differ = new Differ(goldenClassPath, classPath2);
    List<Change> changesetToUnify = differ.changesetToUnify();
    List<Change> expected = Lists.newArrayList();
    expected.add(new NewField(ACC_PUBLIC + ACC_STATIC, "staticObjectField_A", "Ljava/lang/Object;", null, null));
    expected.add(new NewField(ACC_PUBLIC + ACC_STATIC, "staticSelfField_A", "Lcom/jawspeak/unifier/dummy/FieldDifference;", null, null));
    expected.add(new NewField(ACC_PUBLIC + ACC_STATIC, "staticStringField_A", "Ljava/lang/String;", null, null));
    expected.add(new NewField(ACC_PUBLIC + ACC_STATIC, "staticIntField_A", "I", null, null));
    assertEquals(expected, changesetToUnify);
  }

  @Test
  public void sameClassnameDifferentMethods() throws Exception {
    
  }
  
  public static class ClassSubB extends ClassB {
    public String fieldSubB;
  }
  
}
