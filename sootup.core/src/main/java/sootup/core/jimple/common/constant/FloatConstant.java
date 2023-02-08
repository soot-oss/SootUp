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

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Floating point constant with single precision. */
public class FloatConstant implements RealConstant<FloatConstant> {

  private final float value;

  private FloatConstant(@Nonnull float value) {
    this.value = value;
  }

  public static FloatConstant getInstance(@Nonnull float value) {
    return new FloatConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof FloatConstant && Float.compare(((FloatConstant) c).value, value) == 0;
  }

  /** Returns a hash code for this FloatConstant object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(value);
  }

  // PTC 1999/06/28
  @Nonnull
  @Override
  public FloatConstant add(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(value + c.value);
  }

  @Nonnull
  @Override
  public FloatConstant subtract(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(value - c.value);
  }

  @Nonnull
  @Override
  public FloatConstant multiply(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(value * c.value);
  }

  @Nonnull
  @Override
  public FloatConstant divide(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(value / c.value);
  }

  @Nonnull
  @Override
  public FloatConstant remainder(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) == 0);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) != 0);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThan(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) < 0);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThanOrEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) <= 0);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThan(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) > 0);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThanOrEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(value, c.value) >= 0);
  }

  @Nonnull
  @Override
  public IntConstant cmpg(@Nonnull FloatConstant constant) {
    final float cValue = constant.value;
    if (value < cValue) {
      return IntConstant.getInstance(-1);
    } else if (value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(1);
    }
  }

  @Nonnull
  @Override
  public IntConstant cmpl(@Nonnull FloatConstant constant) {
    final float cValue = constant.value;
    if (value > cValue) {
      return IntConstant.getInstance(1);
    } else if (value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @Nonnull
  @Override
  public FloatConstant negate() {
    return FloatConstant.getInstance(-value);
  }

  @Override
  public String toString() {
    String floatString = Float.toString(value);

    if (floatString.equals("NaN")
        || floatString.equals("Infinity")
        || floatString.equals("-Infinity")) {
      return "#" + floatString + "F";
    } else {
      return floatString + "F";
    }
  }

  @Nonnull
  @Override
  public Type getType() {
    return PrimitiveType.getFloat();
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseFloatConstant(this);
  }

  public float getValue() {
    return value;
  }
}
