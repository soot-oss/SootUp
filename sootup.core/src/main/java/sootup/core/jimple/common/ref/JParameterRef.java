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

import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.RefVisitor;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * <code>ParameterRef</code> objects are used by <code>Body</code> objects to refer to the parameter
 * slots on method entry. <br>
 *
 * <p>For instance, in a method, the first statement will often be <code>
 *  this := @parameter0; </code>
 */
public final class JParameterRef implements IdentityRef {

  private final int index;
  private final Type paramType;

  /**
   * Constructs a ParameterRef object of the specified type, representing the specified parameter
   * number.
   */
  public JParameterRef(@NonNull Type paramType, int number) {
    this.index = number;
    this.paramType = paramType;
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
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
  public void toString(@NonNull StmtPrinter up) {
    up.identityRef(this);
  }

  /** Returns the num of this ParameterRef. */
  public int getIndex() {
    return index;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.empty();
  }

  /** Returns the type of this ParameterRef. */
  @NonNull
  @Override
  public Type getType() {
    return paramType;
  }

  @Override
  public <V extends RefVisitor> V accept(@NonNull V v) {

    v.caseParameterRef(this);
    return v;
  }

  @NonNull
  public JParameterRef withParamType(@NonNull Type paramType) {
    return new JParameterRef(paramType, index);
  }

  @NonNull
  public JParameterRef withNumber(int number) {
    return new JParameterRef(paramType, number);
  }
}
