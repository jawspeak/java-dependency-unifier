package com.jawspeak.unifier;

import static com.google.classpath.RegExpResourceFilter.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import com.google.classpath.ResourceFilter;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;


public class FilteringClassPathTest {
  
  ClassPath classpath = Mockito.mock(ClassPath.class);
  FilteringClassPath filteringClassPath = new FilteringClassPath(classpath, new RegExpResourceFilter("com.*", ".*omething.*\\.class"));
  
  @Test
  public void shouldFilterOutClassByPackageAndResourceName() throws Exception {
    when(classpath.findResources(anyString(), (ResourceFilter)anyObject()))
      .thenReturn(new String[] {"com/bar/Junk.class", "com/bar/Something.class", "org/bar/Something.class"});
    assertArrayEquals(new String[] {"com/bar/Something.class"}, 
        filteringClassPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS)));
  }

  @Test
  public void shouldFilterGetResourceAsStream() throws Exception {
    when(classpath.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream(new byte[] {}));
    assertNull(filteringClassPath.getResourceAsStream("org/something/Clazz.class"));
    assertNotNull(filteringClassPath.getResourceAsStream("com/baz/Something.class"));
  }
  
  @Test
  public void shouldNotFilterIsPackage() throws Exception {
    when(classpath.isPackage(anyString())).thenReturn(true);
    assertTrue(filteringClassPath.isPackage("org/bar"));
    assertTrue(filteringClassPath.isPackage("com"));    
  }
  
  @Test
  public void shouldFilterIsResource() {
    when(classpath.isResource(anyString())).thenReturn(true);
    assertFalse(filteringClassPath.isResource("org/baz/Clazz.class"));
    assertTrue(filteringClassPath.isResource("com/bar/Something.class"));
  }

  @Test
  public void shouldNotFilterListPackages() throws Exception {
    String[] packages = new String[] {"com/foo", "org/baz"};
    when(classpath.listPackages(anyString())).thenReturn(packages);
    assertArrayEquals(packages, filteringClassPath.listPackages(""));
  }
  
  @Test
  public void shouldFilterListResources() throws Exception {
    String[] packages = new String[] {"com/foo/Something.class", "org/baz/MyClass.class"};
    when(classpath.listResources(anyString())).thenReturn(packages);
    assertArrayEquals(new String[] {"com/foo/Something.class"}, filteringClassPath.listResources(""));
  }
  
  @Test
  public void shouldSplitResourcesToPackageAndResource() throws Exception {
    FilteringClassPath filteringClassPath = new FilteringClassPath(null, null);
    assertArrayEquals(new String[] {"com/foo", "Baz.class"}, filteringClassPath.splitResourceToPathAndFile("com/foo/Baz.class"));
    assertArrayEquals(new String[] {"com/a/b/c/foo", "Baz.class"}, filteringClassPath.splitResourceToPathAndFile("com/a/b/c/foo/Baz.class"));
    assertArrayEquals(new String[] {"", "Baz.class"}, filteringClassPath.splitResourceToPathAndFile("Baz.class"));
  }
}
