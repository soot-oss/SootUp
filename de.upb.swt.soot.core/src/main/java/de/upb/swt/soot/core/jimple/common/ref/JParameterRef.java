package de.upb.swt.soot.core.jimple.common.ref;

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

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.RefVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * <code>ParameterRef</code> objects are used by <code>Body</code> objects to refer to the parameter
 * slots on methodRef entry. <br>
 *
 * <p>For instance, in an instance methodRef, the first statement will often be <code>
 *  this := @parameter0; </code>
 */
public final class JParameterRef implements IdentityRef, Copyable {

  private final int num;
  private final Type paramType;

  /**
   * Constructs a ParameterRef object of the specified type, representing the specified parameter
   * number.
   */
  public JParameterRef(Type paramType, int number) {
    this.num = number;
    this.paramType = paramType;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseParameterRef(this, o);
  }

  @Override
  public int equivHashCode() {
    return num * 101 + paramType.hashCode() * 17;
  }

  /** Converts the given ParameterRef into a String i.e. <code>@parameter0: .int</code>. */
  @Override
  public String toString() {
    return "@parameter" + num + ": " + paramType;
  }

  @Override
  public void toString(StmtPrinter up) {
    up.identityRef(this);
  }

  /** Returns the index of this ParameterRef. */
  public int getIndex() {
    return num;
  }

  @Override
  public final List<Value> getUses() {
    return Collections.emptyList();
  }

  /** Returns the type of this ParameterRef. */
  @Override
  public Type getType() {
    return paramType;
  }

  /** Used with RefSwitch. */
  @Override
  public void accept(Visitor sw) {
    ((RefVisitor) sw).caseParameterRef(this);
  }

  @Nonnull
  public JParameterRef withParamType(Type paramType) {
    return new JParameterRef(paramType, num);
  }

  @Nonnull
  public JParameterRef withNumber(int number) {
    return new JParameterRef(paramType, number);
  }
}
