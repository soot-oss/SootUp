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

public class IntConstant implements ArithmeticConstant {
  /** */
  private static final long serialVersionUID = 1266232311067376706L;

  private final int value;

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
  public IntConstant add(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value + ((IntConstant) c).value);
  }

  @Override
  public IntConstant subtract(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value - ((IntConstant) c).value);
  }

  @Override
  public IntConstant multiply(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value * ((IntConstant) c).value);
  }

  @Override
  public IntConstant divide(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value / ((IntConstant) c).value);
  }

  @Override
  public IntConstant remainder(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value % ((IntConstant) c).value);
  }

  @Override
  public BooleanConstant equalEqual(ComparableConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return BooleanConstant.getInstance(this.value == ((IntConstant) c).value);
  }

  @Override
  public BooleanConstant notEqual(ComparableConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return BooleanConstant.getInstance(this.value != ((IntConstant) c).value);
  }

  @Override
  public IntConstant lessThan(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value < ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant lessThanOrEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value <= ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant greaterThan(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value > ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant greaterThanOrEqual(NumericConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance((this.value >= ((IntConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant negate() {
    return IntConstant.getInstance(-(this.value));
  }

  @Override
  public IntConstant and(LogicalConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value & ((IntConstant) c).value);
  }

  @Override
  public IntConstant or(LogicalConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value | ((IntConstant) c).value);
  }

  @Override
  public IntConstant xor(LogicalConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value ^ ((IntConstant) c).value);
  }

  @Override
  public IntConstant shiftLeft(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value << ((IntConstant) c).value);
  }

  @Override
  public IntConstant shiftRight(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return IntConstant.getInstance(this.value >> ((IntConstant) c).value);
  }

  @Override
  public IntConstant unsignedShiftRight(ArithmeticConstant c) {
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

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
