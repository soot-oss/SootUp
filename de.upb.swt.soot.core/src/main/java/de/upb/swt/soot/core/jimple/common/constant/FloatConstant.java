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

package de.upb.swt.soot.core.jimple.common.constant;

import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/** Floating point constant with single precision. */
public class FloatConstant implements RealConstant<FloatConstant> {

  private final float value;

  private FloatConstant(float value) {
    this.value = value;
  }

  public static FloatConstant getInstance(float value) {
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

  @Override
  public Type getType() {
    return PrimitiveType.getFloat();
  }

  @Override
  public void accept(Visitor sw) {
    ((ConstantVisitor) sw).caseFloatConstant(this);
  }

  public float getValue() {
    return value;
  }
}
