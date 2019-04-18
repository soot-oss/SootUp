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

public class IntConstant extends ArithmeticConstant {
  /** */
  private static final long serialVersionUID = 1266232311067376706L;

  protected final int value;

  protected IntConstant(int value) {
    this.value = value;
  }

  public static IntConstant getInstance(int value) {
    return new IntConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof IntConstant && ((IntConstant) c).value == value;
  }

  @Override
  public int hashCode() {
    return value;
  }

  // PTC 1999/06/28
  @Override
  public NumericConstant add(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value + ((IntConstant) c).value);
  }

  @Override
  public NumericConstant subtract(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value - ((IntConstant) c).value);
  }

  @Override
  public NumericConstant multiply(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value * ((IntConstant) c).value);
  }

  @Override
  public NumericConstant divide(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value / ((IntConstant) c).value);
  }

  @Override
  public NumericConstant remainder(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value % ((IntConstant) c).value);
  }

  @Override
  public NumericConstant equalEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value == ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant notEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value != ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant lessThan(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value < ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant lessThanOrEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value <= ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThan(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value > ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant greaterThanOrEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value >= ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public NumericConstant negate() {
    return IntConstant.getInstance(-(this.value));
  }

  @Override
  public ArithmeticConstant and(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value & ((IntConstant) c).value);
  }

  @Override
  public ArithmeticConstant or(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value | ((IntConstant) c).value);
  }

  @Override
  public ArithmeticConstant xor(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value ^ ((IntConstant) c).value);
  }

  @Override
  public ArithmeticConstant shiftLeft(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value << ((IntConstant) c).value);
  }

  @Override
  public ArithmeticConstant shiftRight(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value >> ((IntConstant) c).value);
  }

  @Override
  public ArithmeticConstant unsignedShiftRight(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value >>> ((IntConstant) c).value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @Override
  public Type getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseIntConstant(this);
  }

  public int getValue() {
    return value;
  }
}
