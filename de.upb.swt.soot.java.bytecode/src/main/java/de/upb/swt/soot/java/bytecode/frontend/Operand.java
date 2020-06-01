package de.upb.swt.soot.java.bytecode.frontend;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Stack operand.
 *
 * @author Aaloan Miftah
 */
final class Operand {

  @Nonnull final AbstractInsnNode insn;
  @Nonnull final Value value;
  @Nullable Local stack;
  @Nonnull private final List<Value> boxes = new ArrayList<>();

  /**
   * Constructs a new stack operand.
   *
   * @param insn the instruction that produced this operand.
   * @param value the generated value.
   */
  Operand(@Nonnull AbstractInsnNode insn, @Nonnull Value value) {
    this.insn = insn;
    this.value = value;
  }

  /**
   * Removes a value from this operand.
   *
   * @param vb the value box.
   */
  void removeValue(@Nullable Value vb) {
    if (vb == null) {
      return;
    }
    boxes.remove(vb);
  }

  /**
   * Adds a value to this operand.
   *
   * @param vb the value box.
   */
  void addValue(@Nonnull Value vb) {
    boxes.add(vb);
  }

  /** Updates all value boxes registered to this operand. */
  // TODO: [ms] check if method is still necessary
  void updateBoxes() {
    Value val = stackOrValue();
    for (Value vb : boxes) {
      // FIXME: [ms] box removal leftover: ValueBox.$Accessor.setValue(vb, val);
    }
  }

  /**
   * @param <A> type of value to cast to.
   * @return the value.
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  <A> A value() {
    return (A) value;
  }

  /** @return either the stack local allocated for this operand, or its value. */
  @Nonnull
  // TODO [ms]: check: split into to methods? removes condition check and lots of explicit casts to
  // Immediate
  Value stackOrValue() {
    return stack == null ? value : stack;
  }

  /**
   * Determines if this operand is equal to another operand.
   *
   * @param other the other operand.
   * @return {@code true} if this operand is equal to another operand, {@code false} otherwise.
   */
  boolean equivTo(@Nonnull Operand other) {
    if (other.value == null && value == null) {
      return true;
    }
    return stackOrValue().equivTo(other.stackOrValue());
  }

  @Override
  public String toString() {
    return "Operand{"
        + "insn="
        + insn
        + ", value="
        + value
        + ", stack="
        + stack
        + ", boxes="
        + boxes
        + '}';
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Operand && equivTo((Operand) other);
  }
}
