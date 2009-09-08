package com.jawspeak.unifier;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class ClassPathDifferTest {
  
  @Test
  public void recognizeNoDifferecesInSameClassInfos() throws Exception {
    Map<String,ClassInfo> map = ImmutableMap.of("com.jawspeak.MyClass", new ClassInfo("com.jawspeak.MyClass"));
    ClassPathDiffer differ = new ClassPathDiffer(map, map);
    assertEquals(Lists.newArrayList(), differ.changesNeededInBToMatchA());
    assertEquals(Lists.newArrayList(), differ.changesNeededInAToMatchB());
  }
}
