package de.upb.soot.core;

import de.upb.soot.namespaces.classprovider.ClassSource;

/**
 * Soot's counterpart of the source languages class concept.
 *
 * @author Manuel Benz created on 06.06.18
 */

public class SootClass {

  static public class Resolve {
    public final int level;

    public Resolve(int level) {
      this.level = level;
    }
  }

  private ClassSource cs;

  public SootClass(ClassSource cs) {
    this.cs = cs;
  }

  public void resolve() {
    // aktor stuff
    cs.resolve();
  }
}
