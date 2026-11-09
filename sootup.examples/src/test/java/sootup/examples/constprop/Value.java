package sootup.examples.constprop;

import java.util.Objects;

/**
 * A lattice value for constant propagation.
 *
 * <p>The lattice has three levels:
 *
 * <pre>
 *          NAC              (Not A Constant — too many possible values)
 *         /   \
 *   ... c1   c2 ...        (known integer constants)
 *         \   /
 *          UNDEF            (Not yet seen — no information yet)
 * </pre>
 *
 * <p>The meet operator (⊓) chooses the least upper bound:
 *
 * <ul>
 *   <li>NAC ⊓ anything = NAC
 *   <li>UNDEF ⊓ v = v
 *   <li>Constant(a) ⊓ Constant(a) = Constant(a)
 *   <li>Constant(a) ⊓ Constant(b) = NAC when a ≠ b
 * </ul>
 */
// --8<-- [start:lattice-type]
public final class Value {

  private enum Kind {
    UNDEF,
    CONSTANT,
    NAC
  }

  private final Kind kind;
  private final int constant; // meaningful only when kind == CONSTANT

  private Value(Kind kind, int constant) {
    this.kind = kind;
    this.constant = constant;
  }

  public static Value getUndef() {
    return new Value(Kind.UNDEF, 0);
  }

  public static Value getNAC() {
    return new Value(Kind.NAC, 0);
  }

  public static Value makeConstant(int v) {
    return new Value(Kind.CONSTANT, v);
  }

  public boolean isUndef() {
    return kind == Kind.UNDEF;
  }

  public boolean isConstant() {
    return kind == Kind.CONSTANT;
  }

  public boolean isNAC() {
    return kind == Kind.NAC;
  }

  /** Precondition: {@link #isConstant()} must be true. */
  public int getConstant() {
    if (!isConstant()) throw new IllegalStateException("Not a constant: " + this);
    return constant;
  }

  /** Meet operator: returns the least upper bound of this and {@code other}. */
  public Value meet(Value other) {
    if (this.isNAC() || other.isNAC()) return getNAC();
    if (this.isUndef()) return other;
    if (other.isUndef()) return this;
    // Both are constants.
    return this.constant == other.constant ? this : getNAC();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Value)) return false;
    Value v = (Value) o;
    return kind == v.kind && constant == v.constant;
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, constant);
  }

  @Override
  public String toString() {
    return switch (kind) {
      case UNDEF -> "UNDEF";
      case NAC -> "NAC";
      case CONSTANT -> String.valueOf(constant);
    };
  }
}
// --8<-- [end:lattice-type]
