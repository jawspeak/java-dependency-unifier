package com.jawspeak.unifier.changes;

import java.util.Arrays;
import java.util.List;

public class NewClass implements Change {

  private int version;
  private int access;
  private String name;
  private String signature;
  private String superName;
  private String[] interfaces;
  private List<NewMethod> newMethods;
  private List<NewField> newFields;

  public NewClass(int version, int access, String name, String signature, String superName,
      String[] interfaces, List<NewMethod> newMethods, List<NewField> newFields) {
    this.version = version;
    this.access = access;
    this.name = name;
    this.signature = signature;
    this.superName = superName;
    this.interfaces = interfaces;
    this.newMethods = newMethods;
    this.newFields = newFields;
  }

  public void run() {
    
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + access;
    result = prime * result + Arrays.hashCode(interfaces);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((newFields == null) ? 0 : newFields.hashCode());
    result = prime * result + ((newMethods == null) ? 0 : newMethods.hashCode());
    result = prime * result + ((signature == null) ? 0 : signature.hashCode());
    result = prime * result + ((superName == null) ? 0 : superName.hashCode());
    result = prime * result + version;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NewClass other = (NewClass) obj;
    if (access != other.access)
      return false;
    if (!Arrays.equals(interfaces, other.interfaces))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (newFields == null) {
      if (other.newFields != null)
        return false;
    } else if (!newFields.equals(other.newFields))
      return false;
    if (newMethods == null) {
      if (other.newMethods != null)
        return false;
    } else if (!newMethods.equals(other.newMethods))
      return false;
    if (signature == null) {
      if (other.signature != null)
        return false;
    } else if (!signature.equals(other.signature))
      return false;
    if (superName == null) {
      if (other.superName != null)
        return false;
    } else if (!superName.equals(other.superName))
      return false;
    if (version != other.version)
      return false;
    return true;
  }
  
  
  
}
