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

package de.upb.soot.jimple.common.constant;

import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IConstantVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import soot.util.StringTools;

public class StringConstant extends Constant {
  /**
   * 
   */
  private static final long serialVersionUID = -1247456329894136483L;
  public final String value;

  private StringConstant(String s) {
    this.value = s;
  }

  public static StringConstant getInstance(String value) {
    return new StringConstant(value);
  }

  // In this case, equals should be structural equality.
  @Override
  public boolean equals(Object c) {
    return (c instanceof StringConstant && ((StringConstant) c).value.equals(this.value));
  }

  /** Returns a hash code for this StringConstant object. */
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public Type getType() {
    return RefType.getInstance("java.lang.String");
  }

  @Override
  public void accept(IVisitor sw) {
    ((IConstantVisitor) sw).caseStringConstant(this);
  }

  @Override
  public String toString() {
    return StringTools.getQuotedStringOf(value);
  }
}
