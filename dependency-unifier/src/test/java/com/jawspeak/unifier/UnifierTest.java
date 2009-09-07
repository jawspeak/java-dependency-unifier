package com.jawspeak.unifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UnifierTest {
  
  private Unifier unifier = new Unifier();
  private SpyPrintStream spyPrintStream = new SpyPrintStream();

  @Test
  public void invalidArgs() throws Exception {
    assertFalse(unifier.validateArgs(spyPrintStream));
    assertEquals("Usage: java com.jawspeak.unifier.Unifier jarA.jar jarB.jar unified-output-dir/\n",
        spyPrintStream.toString());
    spyPrintStream.clear();
    
    assertFalse(unifier.validateArgs(spyPrintStream, "one.jar"));
    assertEquals("Usage: java com.jawspeak.unifier.Unifier jarA.jar jarB.jar unified-output-dir/\n",
        spyPrintStream.toString());
    spyPrintStream.clear();
    
    assertFalse(unifier.validateArgs(spyPrintStream, "one.jar", "two.jar"));
    assertEquals("Usage: java com.jawspeak.unifier.Unifier jarA.jar jarB.jar unified-output-dir/\n",
        spyPrintStream.toString());
    spyPrintStream.clear();
    
    assertFalse(unifier.validateArgs(spyPrintStream, "1", "2", "3", "4"));
    assertEquals("Usage: java com.jawspeak.unifier.Unifier jarA.jar jarB.jar unified-output-dir/\n",
        spyPrintStream.toString());
    spyPrintStream.clear();
  }

  @Test
  public void validArgs() throws Exception {
    assertTrue(unifier.validateArgs(spyPrintStream, "one.jar", "two.jar", "output-dir"));
  }
}
