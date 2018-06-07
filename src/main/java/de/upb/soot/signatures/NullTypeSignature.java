package de.upb.soot.signatures;

public class NullTypeSignature extends TypeSignature {

  public static final NullTypeSignature NULL_TYPE_SIGNATURE = new NullTypeSignature();

  private NullTypeSignature() {
  }

  @Override
  public String toString() {
    return "null";
  }
}
