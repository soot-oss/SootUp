package de.upb.swt.soot.java.core;

// Modifier for java 9 modules
public enum ModuleModifier {
  OPENS(0x0020), // make a module accessible to reflection (deep&shallow)
  REQUIRES_TRANSITIVE(
      0x0020), // indicates a dependency that is accessible to other modules which require the given
               // module
  REQUIRES_STATIC(0x0040), // static: needed at compile but not necessarily at runtime
  REQUIRES_SYNTHETIC(0x1000), // ?
  REQUIRES_MANDATED(0x8000); // e.g. to java.base

  // USES(0),                       // dependencies which are resolved at runtime
  // PROVIDES(0);

  private final int bytecode;

  ModuleModifier(int i) {
    bytecode = i;
  }

  public int getBytecode() {
    return bytecode;
  }
}
