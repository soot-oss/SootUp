package de.upb.swt.soot.java.core;

// Modifier for java 9 modules
public enum ModuleModifier {
  OPENS(0x0020),
  REQUIRES_TRANSITIVE(0x0020),
  REQUIRES_STATIC(0x0040),
  REQUIRES_SYNTHETIC(0x1000),
  REQUIRES_MANDATED(0x8000);

  // TODO: [ms] check why are those not listed as opcodes?
  // USES(0),
  // PROVIDES(0);

  private final int bytecode;

  ModuleModifier(int i) {
    bytecode = i;
  }

  public int getBytecode() {
    return bytecode;
  }
}
