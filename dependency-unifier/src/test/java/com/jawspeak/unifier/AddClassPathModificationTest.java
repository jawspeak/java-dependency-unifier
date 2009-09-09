package com.jawspeak.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;


public class AddClassPathModificationTest {

  @Test
  public void equals() throws Exception {
    AddClassPathModification modification1 = new AddClassPathModification(new ClassInfo("fqcn"));
    AddClassPathModification modification2 = new AddClassPathModification(new ClassInfo("fqcn"));
    assertEquals(modification1, modification2);
  }
  
  @Test
  public void notEquals() throws Exception {
    AddClassPathModification modification1 = new AddClassPathModification(new ClassInfo("fqcn1"));
    AddClassPathModification modification2 = new AddClassPathModification(new ClassInfo("fqcn2"));
    assertFalse(modification1.equals(modification2));
  }
  
  
}
