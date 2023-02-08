package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann
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

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** A 32-bit integer constant. */
public class IntConstant implements ShiftableConstant<IntConstant> {

  private final int value;

  protected IntConstant(@Nonnull int value) {
    this.value = value;
  }

  public static IntConstant getInstance(@Nonnull int value) {
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
  @Nonnull
  @Override
  public IntConstant add(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value + c.value);
  }

  @Nonnull
  @Override
  public IntConstant subtract(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value - c.value);
  }

  @Nonnull
  @Override
  public IntConstant multiply(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value * c.value);
  }

  @Nonnull
  @Override
  public IntConstant divide(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value / c.value);
  }

  @Nonnull
  @Override
  public IntConstant remainder(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value % c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThan(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant lessThanOrEqual(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThan(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant greaterThanOrEqual(@Nonnull IntConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  @Nonnull
  @Override
  public IntConstant negate() {
    return IntConstant.getInstance(-(value));
  }

  @Nonnull
  @Override
  public IntConstant and(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value & c.value);
  }

  @Nonnull
  @Override
  public IntConstant or(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value | c.value);
  }

  @Nonnull
  @Override
  public IntConstant xor(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value ^ c.value);
  }

  @Nonnull
  @Override
  public IntConstant shiftLeft(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value << c.value);
  }

  @Nonnull
  @Override
  public IntConstant shiftRight(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value >> c.value);
  }

  @Nonnull
  @Override
  public IntConstant unsignedShiftRight(@Nonnull IntConstant c) {
    return IntConstant.getInstance(value >>> c.value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @Nonnull
  @Override
  public Type getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseIntConstant(this);
  }

  public int getValue() {
    return value;
  }
}
