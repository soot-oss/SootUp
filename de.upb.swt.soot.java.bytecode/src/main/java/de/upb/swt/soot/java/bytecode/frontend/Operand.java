package de.upb.swt.soot.java.bytecode.frontend;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Andreas Dann, Christian Brüggemann and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
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

  @Nonnull protected final AbstractInsnNode insn;
  @Nonnull protected final Value value;
  @Nullable protected Local stack;
  @Nonnull protected final List<ValueBox> boxes = new ArrayList<>();

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
  void removeBox(@Nullable ValueBox vb) {
    if (vb == null) {
      return;
    }
    boxes.remove(vb);
  }

  /**
   * Adds a value box to this operand.
   *
   * @param vb the value box.
   */
  void addBox(@Nonnull ValueBox vb) {
    boxes.add(vb);
  }

  /** Updates all value boxes registered to this operand. */
  void updateBoxes() {
    Value val = stackOrValue();
    for (ValueBox vb : boxes) {
      ValueBox.$Accessor.setValue(vb, val);
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

  @Nonnull
  public AbstractInsnNode getInsn() {
    return insn;
  }

  @Nonnull
  public Value getValue() {
    return value;
  }

  @Nullable
  public Local getStack() {
    return stack;
  }

  @Nonnull
  public List<ValueBox> getBoxes() {
    return boxes;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Operand && equivTo((Operand) other);
  }
}
