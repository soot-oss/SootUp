package de.upb.soot.core;

import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.classprovider.ClassSource;

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

  public boolean isPhantom() {
    // TODO Auto-generated method stub
    return false;

  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public SootClass getSuperclassUnsafe() {

    // TODO Auto-generated method stub
    return null;
  }

  public Type getType() {
    // TODO Auto-generated method stub
    return null;
  }
}
