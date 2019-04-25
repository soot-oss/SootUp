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

/** Floating point constant with single precision. */
public class FloatConstant implements RealConstant<FloatConstant> {

  /** */
  private static final long serialVersionUID = 1743530246829003090L;

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
    return FloatConstant.getInstance(this.value + c.value);
  }

  @Nonnull
  @Override
  public FloatConstant subtract(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(this.value - c.value);
  }

  @Nonnull
  @Override
  public FloatConstant multiply(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(this.value * c.value);
  }

  @Nonnull
  @Override
  public FloatConstant divide(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(this.value / c.value);
  }

  @Nonnull
  @Override
  public FloatConstant remainder(@Nonnull FloatConstant c) {
    return FloatConstant.getInstance(this.value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(this.value, c.value) == 0);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull FloatConstant c) {
    return BooleanConstant.getInstance(Float.compare(this.value, c.value) != 0);
  }

  @Nonnull
  @Override
  public IntConstant lessThan(@Nonnull FloatConstant c) {
    return IntConstant.getInstance(Float.compare(this.value, c.value) < 0 ? 1 : 0);
  }

  @Nonnull
  @Override
  public IntConstant lessThanOrEqual(@Nonnull FloatConstant c) {
    return IntConstant.getInstance(Float.compare(this.value, c.value) <= 0 ? 1 : 0);
  }

  @Nonnull
  @Override
  public IntConstant greaterThan(@Nonnull FloatConstant c) {
    return IntConstant.getInstance(Float.compare(this.value, c.value) > 0 ? 1 : 0);
  }

  @Nonnull
  @Override
  public IntConstant greaterThanOrEqual(@Nonnull FloatConstant c) {
    return IntConstant.getInstance(Float.compare(this.value, c.value) >= 0 ? 1 : 0);
  }

  @Override
  public IntConstant cmpg(FloatConstant constant) {
    final float cValue = constant.value;
    if (this.value < cValue) {
      return IntConstant.getInstance(-1);
    } else if (this.value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(1);
    }
  }

  @Override
  public IntConstant cmpl(FloatConstant constant) {
    final float cValue = constant.value;
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
  public FloatConstant negate() {
    return FloatConstant.getInstance(-(this.value));
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
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseFloatConstant(this);
  }

  public float getValue() {
    return value;
  }

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
