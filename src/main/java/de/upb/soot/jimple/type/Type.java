package de.upb.soot.jimple.type;

import java.io.Serializable;

import de.upb.soot.Scene;
import de.upb.soot.core.Numberable;
import de.upb.soot.core.Switchable;
import de.upb.soot.jimple.Switch;

/**
 * Represents types within Soot, eg <code>int</code>, <code>java.lang.String</code>.
 * 
 *
 */
public abstract class Type implements Switchable, Serializable, Numberable {
  public Type() {
    Scene.v().getTypeNumberer().add(this);
  }

  /** Returns a textual representation of this type. */
  @Override
  public abstract String toString();

  /**
   * Returns a textual (and quoted as needed) representation of this type for serialization, e.g. to
   * .jimple format
   */
  public String toQuotedString() {
    return toString();
  }

  /**
   * Returns a textual (and quoted as needed) representation of this type for serialization, e.g. to
   * .jimple format Replaced by toQuotedString; only here for backwards compatibility.
   */
  @Deprecated
  public String getEscapedName() {
    return toQuotedString();
  }

  /** Converts the int-like types (short, byte, boolean and char) to IntType. */
  public static Type toMachineType(Type t) {
    if (t.equals(ShortType.v()) || t.equals(ByteType.v()) || t.equals(BooleanType.v())
        || t.equals(CharType.v())) {
      return IntType.v();
    } else {
      return t;
    }
  }

  /** Returns the least common superclass of this type and other. */
  public Type merge(Type other, Scene cm) {
    // method overriden in subclasses UnknownType and RefType
    throw new RuntimeException("illegal type merge: " + this + " and " + other);
  }

  /** Method required for use of Switchable. */
  @Override
  public void apply(Switch sw) {
  }

  public void setArrayType(ArrayType at) {
    arrayType = at;
  }

  public ArrayType getArrayType() {
    return arrayType;
  }

  public ArrayType makeArrayType() {
    return ArrayType.v(this, 1);
  }

  /**
   * Returns <code>true</code> if this type is allowed to appear in final (clean) Jimple code.
   * 
   * @return
   */
  public boolean isAllowedInFinalCode() {
    return false;
  }

  @Override
  public final int getNumber() {
    return number;
  }

  @Override
  public final void setNumber(int number) {
    this.number = number;
  }

  protected ArrayType arrayType;
  private int number = 0;
}
