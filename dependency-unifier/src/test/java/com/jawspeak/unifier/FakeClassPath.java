package com.jawspeak.unifier;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.classpath.ClassPath;
import com.google.classpath.ResourceFilter;

/**
 * Uses the JVM to load the limited number of classes that are specified as available.
 * Helps when testing and your ClassPath needs to be limited to specific classes. Easier to use
 * this than to do lots of mocking.
 * 
 * <p>I.e. replace Mockito usage as follows with simply using this class:
 * <pre>
 *   when(classPath2.findResources(any(String.class), any(ResourceFilter.class)))
 *       .thenReturn(new String[] {"com/jawspeak/unifier/spike/DiffingTest$ClassB.class"});
 *   when(classPath2.getResourceAsStream("com/jawspeak/unifier/spike/DiffingTest$ClassB.class"))
 *       .thenReturn(stream(ClassB.class));
 * </pre>
 * @author Jonathan Andrew Wolter <jaw@jawspeak.com>
 */
public class FakeClassPath implements ClassPath {

  private Map<String, Class<?>> classResources = new HashMap<String, Class<?>>();

  public FakeClassPath(Class<?>... explicitClassesInPath) {
    this(Arrays.asList(explicitClassesInPath));
  }
  public FakeClassPath(List<Class<?>> explicitClassesInPath) {
    for (Class<?> clazz : explicitClassesInPath) {
      int length = clazz.getCanonicalName().length();
      String resource = clazz.getName().replace('.', '/') + ".class";
      classResources.put(resource, clazz);
    }
  }
  
  /** This always returns all resources. It does not do any filtering / resource selection.
   *  It is only her to implement the interface */
  public String[] findResources(String rootPackageName, ResourceFilter resourceFilter) {
    return listResources(null);
  }

  public InputStream getResourceAsStream(String resource) {
    return stream(classResources.get(resource));
  }

  /** Always false, because this is only used for classes */
  public boolean isPackage(String packageName) {
    return false;
  }

  /** Always true, because this only holds classes */
  public boolean isResource(String resource) {
    return true;
  }

  public String[] listPackages(String string) {
    Set<String> packages = new HashSet<String>();
    for (String className : classResources.keySet()) {
      if (className.lastIndexOf('/') > 0) {
        packages.add(className.substring(0, className.lastIndexOf('/')));
      }
    }
    return packages.toArray(new String[packages.size()]);
  }

  /** Parameter ignored, lists all resources */
  public String[] listResources(String packageName) {
    return classResources.keySet().toArray(new String[classResources.size()]);
  }
  
  private InputStream stream(Class<?> clazz) {
    String resource = clazz.getName().replace('.', '/') + ".class";
    InputStream is = clazz.getClassLoader().getResourceAsStream(resource);
    return is;
  }

}
