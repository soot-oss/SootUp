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

package de.upb.swt.soot.core.jimple.common.constant;

import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.StringTools;

public class StringConstant implements Constant {

  private final String value;
  private final Type type;

  public StringConstant(String str, Type type) {
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

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void accept(Visitor sw) {
    ((ConstantVisitor) sw).caseStringConstant(this);
  }

  @Override
  public String toString() {
    return StringTools.getQuotedStringOf(value);
  }

  public String getValue() {
    return value;
  }
}
