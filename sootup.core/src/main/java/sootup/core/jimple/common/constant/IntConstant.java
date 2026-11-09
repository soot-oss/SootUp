package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** A 32-bit integer constant. */
public final class IntConstant implements ShiftableConstant<IntConstant> {

  private static final IntConstant MINUS_ONE = new IntConstant(-1);
  private static final IntConstant ZERO = new IntConstant(0);
  private static final IntConstant ONE = new IntConstant(1);
  private final int value;

  protected IntConstant(final int value) {
    this.value = value;
  }

  public static IntConstant getInstance(final int value) {
    return switch (value) {
      case -1 -> MINUS_ONE;
      case 0 -> ZERO;
      case 1 -> ONE;
      default -> new IntConstant(value);
    };
  }

  @Override
  public boolean equals(final Object c) {
    return c instanceof IntConstant && ((IntConstant) c).value == value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  // PTC 1999/06/28
  @NonNull
  @Override
  public IntConstant add(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public IntConstant subtract(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public IntConstant multiply(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public IntConstant divide(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public IntConstant remainder(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull final IntConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  @NonNull
  @Override
  public IntConstant negate() {
    return IntConstant.getInstance(-(value));
  }

  @NonNull
  @Override
  public IntConstant and(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value & c.value);
  }

  @NonNull
  @Override
  public IntConstant or(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value | c.value);
  }

  @NonNull
  @Override
  public IntConstant xor(@NonNull final IntConstant c) {
    return IntConstant.getInstance(value ^ c.value);
  }

  @NonNull
  @Override
  public IntConstant shiftLeft(@NonNull final ShiftableConstant<?> c) {
    return IntConstant.getInstance(value << c.getValue().intValue());
  }

  @NonNull
  @Override
  public IntConstant shiftRight(@NonNull final ShiftableConstant<?> c) {
    return IntConstant.getInstance(value >> c.getValue().intValue());
  }

  @NonNull
  @Override
  public IntConstant unsignedShiftRight(@NonNull final ShiftableConstant<?> c) {
    return IntConstant.getInstance(value >>> c.getValue().intValue());
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull final V v) {
    v.caseIntConstant(this);
    return v;
  }

  @NonNull
  @Override
  public Integer getValue() {
    return value;
  }

  public int intValue() {
    return value;
  }
}
