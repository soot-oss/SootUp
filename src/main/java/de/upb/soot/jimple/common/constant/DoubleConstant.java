/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.constant;

import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.types.PrimitiveType;
import de.upb.soot.types.Type;
import javax.annotation.Nonnull;

/** Floating point constant with double precision. */
public class DoubleConstant implements RealConstant<DoubleConstant> {

  /** */
  private static final long serialVersionUID = -6608501059585159445L;

  private final double value;

  private DoubleConstant(double value) {
    this.value = value;
  }

  public static DoubleConstant getInstance(double value) {
    return new DoubleConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return (c instanceof DoubleConstant
        && Double.compare(((DoubleConstant) c).value, this.value) == 0);
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    long v = Double.doubleToLongBits(value);
    return (int) (v ^ (v >>> 32));
  }

  // PTC 1999/06/28
  @Nonnull
  @Override
  public DoubleConstant add(@Nonnull DoubleConstant c) {
    return DoubleConstant.getInstance(this.value + c.value);
  }

  @Nonnull
  @Override
  public DoubleConstant subtract(@Nonnull DoubleConstant c) {
    return DoubleConstant.getInstance(this.value - c.value);
  }

  @Nonnull
  @Override
  public DoubleConstant multiply(@Nonnull DoubleConstant c) {
    return DoubleConstant.getInstance(this.value * c.value);
  }

  @Nonnull
  @Override
  public DoubleConstant divide(@Nonnull DoubleConstant c) {
    return DoubleConstant.getInstance(this.value / c.value);
  }

  @Nonnull
  @Override
  public DoubleConstant remainder(@Nonnull DoubleConstant c) {
    return DoubleConstant.getInstance(this.value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) == 0);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) != 0);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThan(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) < 0);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThanOrEqual(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) <= 0);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThan(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) > 0);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThanOrEqual(@Nonnull DoubleConstant c) {
    return BooleanConstant.getInstance(Double.compare(this.value, c.value) >= 0);
  }

  @Nonnull
  @Override
  public IntConstant cmpg(@Nonnull DoubleConstant constant) {
    final double cValue = constant.value;
    if (this.value < cValue) {
      return IntConstant.getInstance(-1);
    } else if (this.value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(1);
    }
  }

  @Nonnull
  @Override
  public IntConstant cmpl(@Nonnull DoubleConstant constant) {
    final double cValue = constant.value;
    if (this.value > cValue) {
      return IntConstant.getInstance(1);
    } else if (this.value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @Nonnull
  @Override
  public DoubleConstant negate() {
    return DoubleConstant.getInstance(-(this.value));
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

  @Override
  public Type getType() {
    return PrimitiveType.getDouble();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseDoubleConstant(this);
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

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
