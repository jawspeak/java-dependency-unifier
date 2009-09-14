package com.jawspeak.unifier.spike;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;

public class ReadJarWriteJarTest {
  private ClassPath classPath;

  @Test
  public void readsAndThenWritesJar() throws Exception {
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    String[] resources = classPath.findResources("", new RegExpResourceFilter(ANY, ENDS_WITH_CLASS));
    System.out.println("resources=" + Arrays.deepToString(resources));
    
    assertFalse("no dots in path", classPath.isPackage("."));
    assertFalse(classPath.isPackage("com.jawspeak.unifier.dummy"));
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
    
    writeOutFiles("target/created-classes/", resources);
    
    classPath = new ClassPathFactory().createFromPath("src/test/resources/single-class-in-jar.jar");
    assertTrue(classPath.isPackage("/"));
    assertTrue(classPath.isPackage("/com"));
    assertTrue(classPath.isPackage("com"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy"));
    assertTrue(classPath.isPackage("com/jawspeak/unifier/dummy/"));
    assertTrue(classPath.isResource("com/jawspeak/unifier/dummy/DoNothingClass1.class"));
  }

  private void writeOutFiles(String outputDir, String[] resources) throws IOException {
    File outputBase = new File(outputDir);
    outputBase.mkdir();
    for (String resource : resources) {
      String[] pathAndFile = splitResource(resource);
      File output = new File(outputBase, pathAndFile[0]);
      output.mkdirs();
      InputStream is = classPath.getResourceAsStream(resource);
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

      FileOutputStream os = new FileOutputStream(new File(output, pathAndFile[1]));
      os.write(baos.toByteArray());
      os.close();
    }
    
  }

  private String[] splitResource(String resource) {
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
