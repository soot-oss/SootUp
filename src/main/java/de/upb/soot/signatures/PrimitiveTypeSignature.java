package de.upb.soot.signatures;

/** Represents Java's primitive types. */
public class PrimitiveTypeSignature extends TypeSignature {

  public static final PrimitiveTypeSignature BYTE_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("byte");

  public static final PrimitiveTypeSignature SHORT_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("short");

  public static final PrimitiveTypeSignature INT_TYPE_SIGNATURE = new PrimitiveTypeSignature("int");

  public static final PrimitiveTypeSignature LONG_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("long");

  public static final PrimitiveTypeSignature FLOAT_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("float");

  public static final PrimitiveTypeSignature DOUBLE_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("double");

  public static final PrimitiveTypeSignature CHAR_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("char");

  public static final PrimitiveTypeSignature BOOLEAN_TYPE_SIGNATURE =
      new PrimitiveTypeSignature("boolean");

  /** The primitive type's name */
  public final String name;

  /**
   * Signatures of primitive types are unique and should not be created from the outside, thus the
   * constructor is private.
   *
   * @param name the primitive's name
   */
  private PrimitiveTypeSignature(String name) {
    this.name = name;
  }
}
