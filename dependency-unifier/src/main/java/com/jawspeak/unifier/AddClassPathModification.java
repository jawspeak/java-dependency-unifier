package com.jawspeak.unifier;

public class AddClassPathModification implements ClassPathModification {

  private final ClassInfo classInfoToAdd;

  public AddClassPathModification(ClassInfo classInfo) {
    this.classInfoToAdd = classInfo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((classInfoToAdd == null) ? 0 : classInfoToAdd.hashCode());
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
    AddClassPathModification other = (AddClassPathModification) obj;
    if (classInfoToAdd == null) {
      if (other.classInfoToAdd != null)
        return false;
    } else if (!classInfoToAdd.equals(other.classInfoToAdd))
      return false;
    return true;
  }
  
}
