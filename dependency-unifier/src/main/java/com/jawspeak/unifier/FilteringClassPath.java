/**
 * 
 */
package com.jawspeak.unifier;

import java.io.InputStream;
import java.util.List;

import com.google.classpath.ClassPath;
import com.google.classpath.ResourceFilter;
import com.google.common.collect.Lists;

public class FilteringClassPath implements ClassPath {

  private final ResourceFilter filter;
  private final ClassPath composite;

  public FilteringClassPath(ClassPath composite, ResourceFilter filter) {
    this.composite = composite;
    this.filter = filter;
  }
  
  public String[] findResources(String rootPackageName, ResourceFilter resourceFilter) {
    List<String> preFiltered = Lists.newArrayList(composite.findResources(rootPackageName, resourceFilter));
    List<String> filtered = Lists.newArrayList();
    for (String resource : preFiltered) {
      String[] resources = splitResourceToPathAndFile(resource);
      if (filter.match(resources[0], resources[1])) {
        filtered.add(resource);
      }
    }
    return filtered.toArray(new String[filtered.size()]);
  }

  public InputStream getResourceAsStream(String resource) {
    String[] packageAndClass = splitResourceToPathAndFile(resource);
    if (!filter.match(packageAndClass[0], packageAndClass[1])) return null;
    return composite.getResourceAsStream(resource);
  }

  public boolean isPackage(String packageName) {
    return composite.isPackage(packageName);
  }

  public boolean isResource(String resource) {
    String[] packageAndClass = splitResourceToPathAndFile(resource);
    if (!filter.match(packageAndClass[0], packageAndClass[1])) return false;
    return composite.isResource(resource);
  }

  public String[] listPackages(String string) {
    return composite.listPackages(string);
  }

  public String[] listResources(String packageName) {
    List<String> filtered = Lists.newArrayList();
    for(String resource : Lists.newArrayList(composite.listResources(packageName))) {
       String[] packageAndClass = splitResourceToPathAndFile(resource);  
       if (filter.match(packageAndClass[0], packageAndClass[1])) {
         filtered.add(resource);
       }
    }
    return filtered.toArray(new String[filtered.size()]);
  }

  /* visible for test */ String[] splitResourceToPathAndFile(String resource) {
    int lastSlash = resource.lastIndexOf("/");
    if (lastSlash == -1) {
      return new String[] {"", resource};
    }
    return new String[] {resource.substring(0, lastSlash), resource.substring(lastSlash + 1, resource.length())};
  }

}