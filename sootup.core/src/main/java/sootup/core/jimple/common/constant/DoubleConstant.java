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
public class DoubleConstant implements RealConstant<DoubleConstant> {

  private final double value;

  private DoubleConstant(double value) {
    this.value = value;
  }

  public static DoubleConstant getInstance(double value) {
    return new DoubleConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return (c instanceof DoubleConstant && Double.compare(((DoubleConstant) c).value, value) == 0);
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    long v = Double.doubleToLongBits(value);
    return (int) (v ^ (v >>> 32));
  }

  // PTC 1999/06/28
  @NonNull
  @Override
  public DoubleConstant add(@NonNull DoubleConstant c) {
    return DoubleConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public DoubleConstant subtract(@NonNull DoubleConstant c) {
    return DoubleConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public DoubleConstant multiply(@NonNull DoubleConstant c) {
    return DoubleConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public DoubleConstant divide(@NonNull DoubleConstant c) {
    return DoubleConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public DoubleConstant remainder(@NonNull DoubleConstant c) {
    return DoubleConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) == 0);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) != 0);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) < 0);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) <= 0);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) > 0);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(value, c.value) >= 0);
  }

  @NonNull
  @Override
  public IntConstant cmpg(@NonNull DoubleConstant constant) {
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
  public IntConstant cmpl(@NonNull DoubleConstant constant) {
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
    String doubleString = Double.toString(value);

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
  public <V extends ConstantVisitor> V accept(@NonNull V v) {
    v.caseDoubleConstant(this);
    return v;
  }

  /**
   * Checks if passed argument is instance of expected class.
   *
   * @param constant the instance to check
   * @throws IllegalArgumentException when check fails
   */
  private void assertInstanceOf(Constant constant) {
    if (!(constant instanceof DoubleConstant)) {
      throw new IllegalArgumentException("DoubleConstant expected");
    }
  }

  public double getValue() {
    return value;
  }
}
