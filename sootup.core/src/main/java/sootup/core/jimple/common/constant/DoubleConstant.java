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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** Floating point constant with double precision. */
public final class DoubleConstant implements RealConstant<DoubleConstant> {

  private final double value;

  private DoubleConstant(final double value) {
    this.value = value;
  }

  public static DoubleConstant getInstance(final double value) {
    return new DoubleConstant(value);
  }

  @Override
  public boolean equals(final Object c) {
    return c instanceof final DoubleConstant other && value == other.value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    final long v = Double.doubleToLongBits(value);
    return (int) (v ^ (v >>> 32));
  }

  // PTC 1999/06/28
  @NonNull
  @Override
  public DoubleConstant add(@NonNull final DoubleConstant c) {
    return DoubleConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public DoubleConstant subtract(@NonNull final DoubleConstant c) {
    return DoubleConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public DoubleConstant multiply(@NonNull final DoubleConstant c) {
    return DoubleConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public DoubleConstant divide(@NonNull final DoubleConstant c) {
    return DoubleConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public DoubleConstant remainder(@NonNull final DoubleConstant c) {
    return DoubleConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull final DoubleConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  @NonNull
  @Override
  public IntConstant cmpg(@NonNull final DoubleConstant constant) {
    final double cValue = constant.value;
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
  public IntConstant cmpl(@NonNull final DoubleConstant constant) {
    final double cValue = constant.value;
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
  public DoubleConstant negate() {
    return DoubleConstant.getInstance(-(value));
  }

  @Override
  public String toString() {
    final String doubleString = Double.toString(value);

    if (doubleString.equals("NaN")
        || doubleString.equals("Infinity")
        || doubleString.equals("-Infinity")) {
      return "#" + doubleString;
    } else {
      return doubleString;
    }
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getDouble();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull final V v) {
    v.caseDoubleConstant(this);
    return v;
  }

  @NonNull
  @Override
  public Double getValue() {
    return value;
  }

  public double doubleValue() {
    return value;
  }
}
