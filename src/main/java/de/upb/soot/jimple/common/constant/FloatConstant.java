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

import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

/**
 * Floating point constant with single precision.
 */
public class FloatConstant extends RealConstant {

  /**
   * 
   */
  private static final long serialVersionUID = 1743530246829003090L;
  public final float value;

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

  /**
   * Returns a hash code for this FloatConstant object.
   */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(value);
  }

  // PTC 1999/06/28
  @Override
  public NumericConstant add(NumericConstant c) {
    assertInstanceOf(c);
    return FloatConstant.getInstance(this.value + ((FloatConstant) c).value);
  }

  @Override
  public NumericConstant subtract(NumericConstant c) {
    assertInstanceOf(c);
    return FloatConstant.getInstance(this.value - ((FloatConstant) c).value);
  }

  @Override
  public NumericConstant multiply(NumericConstant c) {
    assertInstanceOf(c);
    return FloatConstant.getInstance(this.value * ((FloatConstant) c).value);
  }

  @Override
  public NumericConstant divide(NumericConstant c) {
    assertInstanceOf(c);
    return FloatConstant.getInstance(this.value / ((FloatConstant) c).value);
  }

  @Override
  public NumericConstant remainder(NumericConstant c) {
    assertInstanceOf(c);
    return FloatConstant.getInstance(this.value % ((FloatConstant) c).value);
  }

  @Override
  public NumericConstant equalEqual(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) == 0 ? 1 : 0);
  }

  @Override
  public NumericConstant notEqual(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) != 0 ? 1 : 0);
  }

  @Override
  public NumericConstant lessThan(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) < 0 ? 1 : 0);
  }

  @Override
  public NumericConstant lessThanOrEqual(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) <= 0 ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThan(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) > 0 ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThanOrEqual(NumericConstant c) {
    assertInstanceOf(c);
    return IntConstant.getInstance(Float.compare(this.value, ((FloatConstant) c).value) >= 0 ? 1 : 0);
  }

  @Override
  public IntConstant cmpg(RealConstant constant) {
    assertInstanceOf(constant);
    final float cValue = ((FloatConstant) constant).value;
    if (this.value < cValue) {
      return IntConstant.getInstance(-1);
    } else if (this.value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(1);
    }
  }

  @Override
  public IntConstant cmpl(RealConstant constant) {
    assertInstanceOf(constant);
    final float cValue = ((FloatConstant) constant).value;
    if (this.value > cValue) {
      return IntConstant.getInstance(1);
    } else if (this.value == cValue) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @Override
  public NumericConstant negate() {
    return FloatConstant.getInstance(-(this.value));
  }

  @Override
  public String toString() {
    String floatString = Float.toString(value);

    if (floatString.equals("NaN") || floatString.equals("Infinity") || floatString.equals("-Infinity")) {
      return "#" + floatString + "F";
    } else {
      return floatString + "F";
    }
  }

  @Override
  public Type getType() {
    return FloatType.INSTANCE;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseFloatConstant(this);
  }

  /**
   * Checks if passed argument is instance of expected class.
   *
   * @param constant
   *          the instance to check
   * @throws IllegalArgumentException
   *           when check fails
   */
  private void assertInstanceOf(NumericConstant constant) {
    if (!(constant instanceof FloatConstant)) {
      throw new IllegalArgumentException("FloatConstant expected");
    }
  }

}
