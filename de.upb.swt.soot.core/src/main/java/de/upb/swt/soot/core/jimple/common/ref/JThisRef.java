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

package de.upb.swt.soot.core.jimple.common.ref;

import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.RefVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class JThisRef implements IdentityRef, Copyable {

  private final ReferenceType thisType;

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
  public final List<Value> getUses() {
    return Collections.emptyList();
  }

  @Override
  public Type getType() {
    return thisType;
  }

  @Override
  public void accept(Visitor sw) {
    ((RefVisitor) sw).caseThisRef(this);
  }

  @Nonnull
  public JThisRef withThisType(ReferenceType thisType) {
    return new JThisRef(thisType);
  }
}
