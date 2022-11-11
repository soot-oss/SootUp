package sootup.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann and others
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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/**
 * <code>ParameterRef</code> objects are used by <code>Body</code> objects to refer to the parameter
 * slots on method entry. <br>
 *
 * <p>For instance, in a method, the first statement will often be <code>
 *  this := @parameter0; </code>
 */
public final class JParameterRef implements IdentityRef, Copyable {

  private final int index;
  private final Type paramType;

  /**
   * Constructs a ParameterRef object of the specified type, representing the specified parameter
   * number.
   */
  public JParameterRef(@Nonnull Type paramType, @Nonnull int number) {
    this.index = number;
    this.paramType = paramType;
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseParameterRef(this, o);
  }

  @Override
  public int equivHashCode() {
    return index * 101 + paramType.hashCode() * 17;
  }

  /** Converts the given ParameterRef into a String i.e. <code>@parameter0: .int</code>. */
  @Override
  public String toString() {
    return "@parameter" + index + ": " + paramType;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.identityRef(this);
  }

  /** Returns the num of this ParameterRef. */
  public int getIndex() {
    return index;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    return Collections.emptyList();
  }

  /** Returns the type of this ParameterRef. */
  @Nonnull
  @Override
  public Type getType() {
    return paramType;
  }

  @Override
  public void accept(@Nonnull RefVisitor v) {
    v.caseParameterRef(this);
  }

  @Nonnull
  public JParameterRef withParamType(@Nonnull Type paramType) {
    return new JParameterRef(paramType, index);
  }

  @Nonnull
  public JParameterRef withNumber(@Nonnull int number) {
    return new JParameterRef(paramType, number);
  }
}
