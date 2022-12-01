package sootup.java.core;

// Modifier for java 9 modules corresponding to the bytecodes used in module-info.class
public enum ModuleModifier {
  OPENS(0x0020), // a module is accessible to reflection (deep&shallow)
  REQUIRES_TRANSITIVE(
      0x0020), // indicates a dependency that is accessible to other modules which require the
  // module
  REQUIRES_STATIC(0x0040), // static: needed at compile time but not necessarily at run time
  REQUIRES_SYNTHETIC(0x1000), // ?
  REQUIRES_MANDATED(0x8000); // e.g. (i.e.?) implicit dependenciy to java.base

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
