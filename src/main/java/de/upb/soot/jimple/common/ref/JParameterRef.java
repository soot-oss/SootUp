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

import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * <code>ParameterRef</code> objects are used by <code>Body</code> objects to refer to the parameter slots on method entry.
 * <br>
 * <p>
 * For instance, in an instance method, the first statement will often be <code> this := @parameter0; </code>
 * </p>
 */
public class JParameterRef implements IdentityRef {
  /**
   * 
   */
  private static final long serialVersionUID = -5198809451267425640L;
  private int num;
  private Type paramType;

  /** Constructs a ParameterRef object of the specified type, representing the specified parameter number. */
  public JParameterRef(Type paramType, int number) {
    this.num = number;
    this.paramType = paramType;
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JParameterRef) {
      return num == ((JParameterRef) o).num && paramType.equals(((JParameterRef) o).paramType);
    }
    return false;
  }

  @Override
  public int equivHashCode() {
    return num * 101 + paramType.hashCode() * 17;
  }

  /** Create a new ParameterRef object with the same paramType and number. */
  @Override
  public Object clone() {
    return new JParameterRef(paramType, num);
  }

  /** Converts the given ParameterRef into a String i.e. <code>@parameter0: .int</code>. */
  @Override
  public String toString() {
    return "@parameter" + num + ": " + paramType;
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.identityRef(this);
  }

  /** Returns the index of this ParameterRef. */
  public int getIndex() {
    return num;
  }

  /** Sets the index of this ParameterRef. */
  public void setIndex(int index) {
    num = index;
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
  public void accept(IVisitor sw) {
    // TODO
  }

  @Override
  public boolean equivTo(Object o, Comparator<? extends Object> comparator) {
    // TODO Auto-generated method stub
    return false;
  }
}
