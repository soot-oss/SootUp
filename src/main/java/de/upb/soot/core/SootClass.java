package de.upb.soot.core;

import de.upb.soot.classprovider.ClassSource;
import de.upb.soot.jimple.common.type.Type;

/**
 * Soot's counterpart of the source languages class concept.
 *
 * @author Manuel Benz created on 06.06.18
 */

public class SootClass {

  public static class Resolve {
    public final int level;

    public Resolve(int level) {
      this.level = level;
    }
  }

  public de.upb.soot.classprovider.ClassSource getCs() {
    return cs;
  }

  private ClassSource cs;

  public static final String HIERARCHY = null;
  public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = null;

  public SootClass(ClassSource cs) {
    this.cs = cs;
  }

  public void checkLevelIgnoreResolving(String hierarchy2) {

    // TODO Auto-generated method stub
  }

  public boolean isInterface() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * Whether the class is phantom.
   * 
   * @return
   */
  public boolean isPhantom() {
    // TODO Auto-generated method stub
    return false;

  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Super Class.
   * 
   * @return the superclass
   */
  public SootClass getSuperclassUnsafe() {

    // TODO Auto-generated method stub
    return null;
  }

  public Type getType() {
    // TODO Auto-generated method stub
    return null;
  }
}
