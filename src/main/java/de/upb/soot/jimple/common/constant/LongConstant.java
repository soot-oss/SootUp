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

public class LongConstant implements ArithmeticConstant {
  /** */
  private static final long serialVersionUID = -3227009524415387793L;

  private final long value;

  private LongConstant(long value) {
    this.value = value;
  }

  public static LongConstant getInstance(long value) {
    return new LongConstant(value);
  }

  @Override
  public boolean equals(Object c) {
    return c instanceof LongConstant && ((LongConstant) c).value == this.value;
  }

  /** Returns a hash code for this DoubleConstant object. */
  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  // PTC 1999/06/28
  @Override
  public LongConstant add(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value + ((LongConstant) c).value);
  }

  @Override
  public LongConstant subtract(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value - ((LongConstant) c).value);
  }

  @Override
  public LongConstant multiply(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value * ((LongConstant) c).value);
  }

  @Override
  public LongConstant divide(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value / ((LongConstant) c).value);
  }

  @Override
  public LongConstant remainder(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value % ((LongConstant) c).value);
  }

  @Override
  public BooleanConstant equalEqual(ComparableConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return BooleanConstant.getInstance(this.value == ((LongConstant) c).value);
  }

  @Override
  public BooleanConstant notEqual(ComparableConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return BooleanConstant.getInstance(this.value != ((LongConstant) c).value);
  }

  @Override
  public IntConstant lessThan(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return IntConstant.getInstance((this.value < ((LongConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant lessThanOrEqual(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return IntConstant.getInstance((this.value <= ((LongConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant greaterThan(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return IntConstant.getInstance((this.value > ((LongConstant) c).value) ? 1 : 0);
  }

  @Override
  public IntConstant greaterThanOrEqual(NumericConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return IntConstant.getInstance((this.value >= ((LongConstant) c).value) ? 1 : 0);
  }

  /** Compares the value of LongConstant. */
  public IntConstant cmp(LongConstant c) {
    if (this.value > c.value) {
      return IntConstant.getInstance(1);
    } else if (this.value == c.value) {
      return IntConstant.getInstance(0);
    } else {
      return IntConstant.getInstance(-1);
    }
  }

  @Override
  public LongConstant negate() {
    return LongConstant.getInstance(-(this.value));
  }

  @Override
  public LongConstant and(LogicalConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value & ((LongConstant) c).value);
  }

  @Override
  public LongConstant or(LogicalConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value | ((LongConstant) c).value);
  }

  @Override
  public LongConstant xor(LogicalConstant c) {
    if (!(c instanceof LongConstant)) {
      throw new IllegalArgumentException("LongConstant expected");
    }
    return LongConstant.getInstance(this.value ^ ((LongConstant) c).value);
  }

  @Override
  public LongConstant shiftLeft(ArithmeticConstant c) {
    // NOTE CAREFULLY: the RHS of a shift op is not (!)
    // of Long type. It is, in fact, an IntConstant.

    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return LongConstant.getInstance(this.value << ((IntConstant) c).getValue());
  }

  @Override
  public LongConstant shiftRight(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return LongConstant.getInstance(this.value >> ((IntConstant) c).getValue());
  }

  @Override
  public LongConstant unsignedShiftRight(ArithmeticConstant c) {
    if (!(c instanceof IntConstant)) {
      throw new IllegalArgumentException("IntConstant expected");
    }
    return LongConstant.getInstance(this.value >>> ((IntConstant) c).getValue());
  }

  @Override
  public String toString() {
    return value + "L";
  }

  @Override
  public Type getType() {
    return PrimitiveType.getLong();
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseLongConstant(this);
  }

  public long getValue() {
    return value;
  }

  @Override
  public Object clone() {
    throw new RuntimeException();
  }
}
