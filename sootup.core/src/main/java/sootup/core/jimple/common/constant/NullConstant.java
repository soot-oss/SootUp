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
import sootup.core.types.NullType;
import sootup.core.types.Type;

public class NullConstant implements Constant {

  private static final NullConstant INSTANCE = new NullConstant();

  private NullConstant() {}

  public static NullConstant getInstance() {
    return INSTANCE;
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object c) {
    return c == getInstance();
  }

  @Override
  public int hashCode() {
    return 982;
  }

  @Nonnull
  @Override
  public Type getType() {
    return NullType.getInstance();
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseNullConstant(this);
  }

  @Override
  public String toString() {
    return getType().toString();
  }
}
