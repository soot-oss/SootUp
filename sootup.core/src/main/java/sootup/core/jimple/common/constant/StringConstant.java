package sootup.core.jimple.common.constant;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo and others
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
import sootup.core.jimple.Jimple;
import sootup.core.jimple.visitor.ConstantVisitor;
import sootup.core.types.Type;

public class StringConstant implements Constant {

  private final String value;
  private final Type type;

  public StringConstant(@Nonnull String str, @Nonnull Type type) {
    this.type = type;
    this.value = str;
  }

  // In this case, equals should be structural equality.
  @Override
  public boolean equals(Object c) {
    return (c instanceof StringConstant && ((StringConstant) c).value.equals(value));
  }

  /** Returns a hash code for this StringConstant object. */
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Nonnull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void accept(@Nonnull ConstantVisitor v) {
    v.caseStringConstant(this);
  }

  @Override
  public String toString() {
    final String escape = Jimple.escape(value);
    if (escape.charAt(0) != '\"') {
      return "\"" + value + "\"";
    }
    return escape;
  }

  public String getValue() {
    return value;
  }
}
