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

public class LongConstant implements ShiftableConstant<LongConstant> {
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
  @Nonnull
  @Override
  public LongConstant add(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value + c.value);
  }

  @Nonnull
  @Override
  public LongConstant subtract(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value - c.value);
  }

  @Nonnull
  @Override
  public LongConstant multiply(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value * c.value);
  }

  @Nonnull
  @Override
  public LongConstant divide(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value / c.value);
  }

  @Nonnull
  @Override
  public LongConstant remainder(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value == c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value != c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThan(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value < c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThanOrEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value <= c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThan(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value > c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThanOrEqual(@Nonnull LongConstant c) {
    return BooleanConstant.getInstance(this.value >= c.value);
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

  @Nonnull
  @Override
  public LongConstant negate() {
    return LongConstant.getInstance(-(this.value));
  }

  @Nonnull
  @Override
  public LongConstant and(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value & c.value);
  }

  @Nonnull
  @Override
  public LongConstant or(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value | c.value);
  }

  @Nonnull
  @Override
  public LongConstant xor(@Nonnull LongConstant c) {
    return LongConstant.getInstance(this.value ^ c.value);
  }

  @Nonnull
  @Override
  public LongConstant shiftLeft(@Nonnull IntConstant c) {
    return LongConstant.getInstance(this.value << c.getValue());
  }

  @Nonnull
  @Override
  public LongConstant shiftRight(@Nonnull IntConstant c) {
    return LongConstant.getInstance(this.value >> c.getValue());
  }

  @Nonnull
  @Override
  public LongConstant unsignedShiftRight(@Nonnull IntConstant c) {
    return LongConstant.getInstance(this.value >>> c.getValue());
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
