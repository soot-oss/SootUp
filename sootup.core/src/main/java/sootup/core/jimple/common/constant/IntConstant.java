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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

/** A 32-bit integer constant. */
public class IntConstant implements ShiftableConstant<IntConstant> {

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
  @NonNull
  @Override
  public IntConstant add(@NonNull IntConstant c) {
    return IntConstant.getInstance(value + c.value);
  }

  @NonNull
  @Override
  public IntConstant subtract(@NonNull IntConstant c) {
    return IntConstant.getInstance(value - c.value);
  }

  @NonNull
  @Override
  public IntConstant multiply(@NonNull IntConstant c) {
    return IntConstant.getInstance(value * c.value);
  }

  @NonNull
  @Override
  public IntConstant divide(@NonNull IntConstant c) {
    return IntConstant.getInstance(value / c.value);
  }

  @NonNull
  @Override
  public IntConstant remainder(@NonNull IntConstant c) {
    return IntConstant.getInstance(value % c.value);
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThan(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value < c.value);
  }

  @NonNull
  @Override
  public BooleanConstant lessThanOrEqual(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value <= c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThan(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value > c.value);
  }

  @NonNull
  @Override
  public BooleanConstant greaterThanOrEqual(@NonNull IntConstant c) {
    return BooleanConstant.getInstance(value >= c.value);
  }

  @NonNull
  @Override
  public IntConstant negate() {
    return IntConstant.getInstance(-(value));
  }

  @NonNull
  @Override
  public IntConstant and(@NonNull IntConstant c) {
    return IntConstant.getInstance(value & c.value);
  }

  @NonNull
  @Override
  public IntConstant or(@NonNull IntConstant c) {
    return IntConstant.getInstance(value | c.value);
  }

  @NonNull
  @Override
  public IntConstant xor(@NonNull IntConstant c) {
    return IntConstant.getInstance(value ^ c.value);
  }

  @NonNull
  @Override
  public IntConstant shiftLeft(@NonNull IntConstant c) {
    return IntConstant.getInstance(value << c.value);
  }

  @NonNull
  @Override
  public IntConstant shiftRight(@NonNull IntConstant c) {
    return IntConstant.getInstance(value >> c.value);
  }

  @NonNull
  @Override
  public IntConstant unsignedShiftRight(@NonNull IntConstant c) {
    return IntConstant.getInstance(value >>> c.value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getInt();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull V v) {
    v.caseIntConstant(this);
    return v;
  }

  public int getValue() {
    return value;
  }
}
