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
import de.upb.soot.types.ReferenceType;
import de.upb.soot.types.Type;
import de.upb.soot.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;

public class JThisRef implements IdentityRef {
  /** */
  private static final long serialVersionUID = 5300244196056992260L;

  ReferenceType thisType;

  public JThisRef(ReferenceType thisType) {
    this.thisType = thisType;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseThisRef(this, o);
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
  public void toString(StmtPrinter up) {
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
  public void accept(Visitor sw) {
    // TODO
  }

  @Override
  public Object clone() {
    return new JThisRef(thisType);
  }
}
