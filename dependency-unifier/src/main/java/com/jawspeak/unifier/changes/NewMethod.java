package com.jawspeak.unifier.changes;

public class NewMethod implements Change {

  private final int access;
  private final String name;
  private final String desc;
  private final String signature;
  private final Object value;

  public NewMethod(int access, String name, String desc, String signature, Object value) {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.signature = signature;
    this.value = value;
  }

  public void run() {
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + access;
    result = prime * result + ((desc == null) ? 0 : desc.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((signature == null) ? 0 : signature.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    NewMethod other = (NewMethod) obj;
    if (access != other.access)
      return false;
    if (desc == null) {
      if (other.desc != null)
        return false;
    } else if (!desc.equals(other.desc))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (signature == null) {
      if (other.signature != null)
        return false;
    } else if (!signature.equals(other.signature))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }
  
  

}
