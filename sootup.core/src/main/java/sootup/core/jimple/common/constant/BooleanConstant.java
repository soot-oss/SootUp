package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Linghui Luo, Christian Brüggemann, Markus Schmidt
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

/**
 * BooleanConstant didn't exist in old soot, because in Java byte code boolean values are
 * represented as integer values 1 or 0. However, from the source code we have the information if a
 * constant is boolean or not, adding this class is helpful for setting type of boolean variables.
 *
 * @author Linghui Luo
 */
public class BooleanConstant
    implements LogicalConstant<BooleanConstant>, ComparableConstant<BooleanConstant> {

  private static final BooleanConstant FALSE = new BooleanConstant(false);
  private static final BooleanConstant TRUE = new BooleanConstant(true);

  private final boolean value;

  private BooleanConstant(boolean value) {
    this.value = value;
  }

  public static BooleanConstant getInstance(boolean value) {
    return value ? TRUE : FALSE;
  }

  public static BooleanConstant getInstance(int value) {
    if (value == 1) {
      return TRUE;
    } else if (value == 0) {
      return FALSE;
    }
    throw new RuntimeException("The value of boolean constant can only be 1 or 0");
  }

  public static BooleanConstant getTrue() {
    return TRUE;
  }

  public static BooleanConstant getFalse() {
    return FALSE;
  }

  @NonNull
  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  @Override
  public <V extends ConstantVisitor> V accept(@NonNull V v) {
    v.caseBooleanConstant(this);
    return v;
  }

  @NonNull
  @Override
  public BooleanConstant equalEqual(@NonNull BooleanConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @NonNull
  @Override
  public BooleanConstant notEqual(@NonNull BooleanConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @NonNull
  public BooleanConstant and(@NonNull BooleanConstant c) {
    return BooleanConstant.getInstance(value & c.value);
  }

  @NonNull
  @Override
  public BooleanConstant or(@NonNull BooleanConstant c) {
    return BooleanConstant.getInstance(value | c.value);
  }

  @NonNull
  @Override
  public BooleanConstant xor(@NonNull BooleanConstant c) {
    return BooleanConstant.getInstance(value ^ c.value);
  }

  @Override
  public String toString() {
    return value ? "1" : "0";
  }
}
