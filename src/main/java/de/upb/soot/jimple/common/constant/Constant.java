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

import de.upb.soot.jimple.basic.Immediate;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.Collections;
import java.util.List;

public abstract class Constant implements Value, Immediate {
  /**
   * 
   */
  private static final long serialVersionUID = -6401333027090308367L;

  @Override
  public final List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  /** Clones the current constant. Not implemented here. */
  @Override
  public Object clone() {
    throw new RuntimeException();
  }

  /**
   * Returns true if this object is structurally equivalent to c. For Constants, equality is structural equality, so we just
   * call equals().
   */
  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseConstant(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseConstant(this, o);
  }

  /**
   * Returns a hash code consistent with structural equality for this object. For Constants, equality is structural equality;
   * we hope that each subclass defines hashCode() correctly.
   */
  @Override
  public int equivHashCode() {
    return hashCode();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.constant(this);
  }
}
