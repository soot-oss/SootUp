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

/** Floating point constant with single precision. */
public final class FloatConstant implements RealConstant<FloatConstant> {

  private final float value;

  private FloatConstant(final float value) {
    this.value = value;
  }

  public static FloatConstant getInstance(final float value) {
    return new FloatConstant(value);
  }

  @Override
  public boolean equals(final Object c) {
    return c instanceof final FloatConstant other && value == other.value;
  }

  /** Returns a hash code for this FloatConstant object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(value);
  }

  // PTC 1999/06/28
  @NonNull
  @Override
  public FloatConstant add(@NonNull final FloatConstant c) {
    return FloatConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public FloatConstant subtract(@NonNull final FloatConstant c) {
    return FloatConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public FloatConstant multiply(@NonNull final FloatConstant c) {
    return FloatConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public FloatConstant divide(@NonNull final FloatConstant c) {
    return FloatConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public FloatConstant remainder(@NonNull final FloatConstant c) {
    return FloatConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull final FloatConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  @NonNull
  @Override
  public IntConstant cmpg(@NonNull final FloatConstant constant) {
    final float cValue = constant.value;
    if (value < cValue) {
      return IntConstant.getInstance(-1);
    } else if (value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(1);
    }
  }

  @NonNull
  @Override
  public IntConstant cmpl(@NonNull final FloatConstant constant) {
    final float cValue = constant.value;
    if (value > cValue) {
      return IntConstant.getInstance(1);
    } else if (value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @NonNull
  @Override
  public FloatConstant negate() {
    return FloatConstant.getInstance(-value);
  }

  @Override
  public String toString() {
    final String floatString = Float.toString(value);

    if (floatString.equals("NaN")
        || floatString.equals("Infinity")
        || floatString.equals("-Infinity")) {
      return "#" + floatString + "F";
    } else {
      return floatString + "F";
    }
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getFloat();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull final V v) {
    v.caseFloatConstant(this);
    return v;
  }

  @NonNull
  @Override
  public Float getValue() {
    return value;
  }

  public float floatValue() {
    return value;
  }
}
