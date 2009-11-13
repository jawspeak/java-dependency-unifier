package com.jawspeak.unifier;

public class ClassInfo {

  final String fullyQualifiedClassName;

  public ClassInfo(String fullyQualifiedClassName) {
    this.fullyQualifiedClassName = fullyQualifiedClassName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((fullyQualifiedClassName == null) ? 0 : fullyQualifiedClassName.hashCode());
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
    ClassInfo other = (ClassInfo) obj;
    if (fullyQualifiedClassName == null) {
      if (other.fullyQualifiedClassName != null)
        return false;
    } else if (!fullyQualifiedClassName.equals(other.fullyQualifiedClassName))
      return false;
    return true;
  }

  
}
