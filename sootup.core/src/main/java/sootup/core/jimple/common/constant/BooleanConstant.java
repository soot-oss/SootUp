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

import javax.annotation.Nonnull;
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

  private BooleanConstant(@Nonnull boolean value) {
    this.value = value;
  }

  public static BooleanConstant getInstance(@Nonnull boolean value) {
    return value ? TRUE : FALSE;
  }

  public static BooleanConstant getInstance(@Nonnull int value) {
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

  @Nonnull
  @Override
  public Type getType() {
    return PrimitiveType.getBoolean();
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseBooleanConstant(this);
  }

  @Nonnull
  @Override
  public BooleanConstant equalEqual(@Nonnull BooleanConstant c) {
    return BooleanConstant.getInstance(value == c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant notEqual(@Nonnull BooleanConstant c) {
    return BooleanConstant.getInstance(value != c.value);
  }

  @Nonnull
  public BooleanConstant and(@Nonnull BooleanConstant c) {
    return BooleanConstant.getInstance(value & c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant or(@Nonnull BooleanConstant c) {
    return BooleanConstant.getInstance(value | c.value);
  }

  @Nonnull
  @Override
  public BooleanConstant xor(@Nonnull BooleanConstant c) {
    return BooleanConstant.getInstance(value ^ c.value);
  }

  @Override
  public String toString() {
    return value ? "1" : "0";
  }
}
