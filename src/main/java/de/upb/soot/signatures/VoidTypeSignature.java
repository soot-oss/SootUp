package de.upb.soot.signatures;

/** Represents Java's 'void' type as methods return's type. */
public class VoidTypeSignature extends TypeSignature {

  public static final VoidTypeSignature VOID_TYPE_SIGNATURE = new VoidTypeSignature();

  private VoidTypeSignature() {}

  @Override
  public String toString() {
    return "void";
  }
}
