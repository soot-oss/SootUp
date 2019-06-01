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

package de.upb.soot.jimple.common.ref;

import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.types.Type;
import de.upb.soot.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;

/**
 * <code>ParameterRef</code> objects are used by <code>Body</code> objects to refer to the parameter
 * slots on methodRef entry. <br>
 *
 * <p>For instance, in an instance methodRef, the first statement will often be <code>
 *  this := @parameter0; </code>
 */
public class JParameterRef implements IdentityRef {
  /** */
  private static final long serialVersionUID = -5198809451267425640L;

  private final int num;
  private Type paramType;

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
  public final List<ValueBox> getUseBoxes() {
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
    // TODO
  }
}
