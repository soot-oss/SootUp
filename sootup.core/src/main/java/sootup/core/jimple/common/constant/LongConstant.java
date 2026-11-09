package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann and others
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

/** A 64-bit integer constant */
public final class LongConstant implements ShiftableConstant<LongConstant> {

  private final long value;

  private LongConstant(final long value) {
    this.value = value;
  }

  public static LongConstant getInstance(final long value) {
    return new LongConstant(value);
  }

  @Override
  public boolean equals(final Object c) {
    return c instanceof LongConstant && ((LongConstant) c).value == value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  // PTC 1999/06/28
  @NonNull
  @Override
  public LongConstant add(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public LongConstant subtract(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public LongConstant multiply(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public LongConstant divide(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public LongConstant remainder(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull final LongConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  /** Compares the value of LongConstant. */
  public IntConstant cmp(final LongConstant c) {
    if (value > c.value) {
      return IntConstant.getInstance(1);
    } else if (value == c.value) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @NonNull
  @Override
  public LongConstant negate() {
    return LongConstant.getInstance(-(value));
  }

  @NonNull
  @Override
  public LongConstant and(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value & c.value);
  }

  @NonNull
  @Override
  public LongConstant or(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value | c.value);
  }

  @NonNull
  @Override
  public LongConstant xor(@NonNull final LongConstant c) {
    return LongConstant.getInstance(value ^ c.value);
  }

  @NonNull
  @Override
  public LongConstant shiftLeft(@NonNull final ShiftableConstant<?> c) {
    return LongConstant.getInstance(value << c.getValue().intValue());
  }

  @NonNull
  @Override
  public LongConstant shiftRight(@NonNull final ShiftableConstant<?> c) {
    return LongConstant.getInstance(value >> c.getValue().intValue());
  }

  @NonNull
  @Override
  public LongConstant unsignedShiftRight(@NonNull final ShiftableConstant<?> c) {
    return LongConstant.getInstance(value >>> c.getValue().intValue());
  }

  @Override
  public String toString() {
    return value + "L";
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getLong();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull final V v) {
    v.caseLongConstant(this);
    return v;
  }

  @NonNull
  @Override
  public Long getValue() {
    return value;
  }

  public long longValue() {
    return value;
  }
}
