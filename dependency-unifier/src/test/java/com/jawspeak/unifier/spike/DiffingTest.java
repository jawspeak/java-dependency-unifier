package com.jawspeak.unifier.spike;

public class DiffingTest {

  /*
   * I have a few implementation choices: 
   *   (1) Use java reflection to walk the API and build a representation of the classes.
   *     This would be DOM/Tree based.
   *   (2) Use ASM to visit all methods and accumulate information about the classes. This 
   *     Would be event based.
   * Basically, it's a decision of using SAX vs. DOM api's. 
   * 
   * (1) If using DOM, algo would look like:
   * 
   * Create a Map<Class or fully qualified classname String, ClassInfo>
   * For every class in classpath1:
   *   Record every Class, as a ClassInfo object.
   *   Record every Field. (Type, visibility, name, generics (? if supported later), etc -- what asm wants). 
   *   Record every method.
   *   Record every constructor.
   *   Record every interface it implements.
   * Repeat in classpath2.
   *   (see above)
   * For all of these Classes, translate into ASM friendly objects, or have my Builders translate
   * into the ASM types.
   * 
   * (Assuming we want classapth2 to look LIKE classpath1:
   *  For every class in classpath1:
   *      If the class A exists in classpath2?
   *        For every field/method in class A add if missing on class A' in classpath2
   *      Else:
   *        Re-implement class A with UOE's for the methods, and nulls for fields.
   *          
   *          
   * (2)  The event based model would be similar:
   * View every resource, and record their attributes in an ASM style way. 
   * Then, do the same diffing intermediate step. 
   * This sounds the simplest, as it will involve the least amount of translation/transformation
   * between differnt API/Domains (I just use ASM's types everywhere).
   *  
   */
}
