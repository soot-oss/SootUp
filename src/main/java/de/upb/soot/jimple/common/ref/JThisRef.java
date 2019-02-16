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
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JThisRef implements IdentityRef {
  /**
   * 
   */
  private static final long serialVersionUID = 5300244196056992260L;
  RefType thisType;

  public JThisRef(RefType thisType) {
    this.thisType = thisType;
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JThisRef) {
      return thisType.equals(((JThisRef) o).thisType);
    }
    return false;
  }

  @Override
  public int equivHashCode() {
    return thisType.hashCode();
  }

  @Override
  public String toString() {
    return "@this: " + thisType;
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.identityRef(this);
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  @Override
  public Type getType() {
    return thisType;
  }

  @Override
  public void accept(IVisitor sw) {
    // TODO
  }

  @Override
  public Object clone() {
    return new JThisRef(thisType);
  }

  @Override
  public boolean equivTo(Object o, Comparator<Object> comparator) {
    return comparator.compare(this, o) == 0;
  }

}
