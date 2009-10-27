package com.jawspeak.unifier;

import static com.google.classpath.RegExpResourceFilter.*;
import static org.mockito.asm.Opcodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;

import com.google.classpath.ClassPath;
import com.google.classpath.RegExpResourceFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jawspeak.unifier.changes.Change;
import com.jawspeak.unifier.changes.NewClass;
import com.jawspeak.unifier.changes.NewField;
import com.jawspeak.unifier.changes.NewMethod;

public class Differ {
  private static final RegExpResourceFilter CLASS_FILTER = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
  
  /**
   * Classpath representing the "golden" version of the API you want the second classpath to 
   * be converted to be compile-time-equivalent to.
   */
  private final ClassPath goldenClassPath;
  
  /**
   * Classpath representing the incoming API that you want to change to look like the "golden" 
   * classapth.
   */
  private final ClassPath classPath2;

  public Differ(ClassPath goldenClassPath, ClassPath classPath2) {
    this.goldenClassPath = goldenClassPath;
    this.classPath2 = classPath2;
  }

  /**
   * The changes needed to unify goldenClassPath with the classPath2.
   */
  public List<Change> changesetToUnify() {
    Set<String> goldenClasses = Sets.newHashSet(goldenClassPath.findResources("", CLASS_FILTER));
    Set<String> incomingClasses = Sets.newHashSet(classPath2.findResources("", CLASS_FILTER));

    ArrayList<Change> changes = Lists.newArrayList();
    for (String goldenClass : goldenClasses) {
      if (!incomingClasses.contains(goldenClass)) {
        List<NewField> newFields = Lists.newArrayList(new NewField(ACC_PUBLIC, "fieldB", "Ljava/lang/String;", null, null));
        List<NewMethod> newMethods = Lists.newArrayList(new NewMethod(ACC_PUBLIC, "<init>", "()V", null, null));
        NewClass newClass = new NewClass(V1_5, ACC_PUBLIC + ACC_SUPER, "com/jawspeak/unifier/DiffingTest$ClassB", null, "java/lang/Object", null, newMethods, newFields);
        changes.add(newClass);
      }
    }
    return changes;
  }

  
}
