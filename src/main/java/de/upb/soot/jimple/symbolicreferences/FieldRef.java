package de.upb.soot.jimple.symbolicreferences;

import de.upb.soot.core.SootField;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.ISignature;

public class FieldRef implements SymbolicRef<SootField> {

  private final FieldSignature fieldSignature;
  private final boolean isStatic;

  public FieldRef(FieldSignature fieldSignature, boolean isStatic) {
    this.fieldSignature = fieldSignature;
    this.isStatic = isStatic;
  }

  @Override
  public SootField resolve() {
    return null;
  }

  @Override
  public FieldSignature getSignature() {
    return fieldSignature;
  }
}
