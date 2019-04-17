/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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

package de.upb.soot.jimple.common.expr;

import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractInstanceInvokeExpr extends AbstractInvokeExpr {
  /** */
  private static final long serialVersionUID = 5554270441921308784L;

  protected final ValueBox baseBox;

  protected AbstractInstanceInvokeExpr(
      ValueBox baseBox, MethodSignature methodSig, ValueBox[] argBoxes) {
    super(methodSig, argBoxes);
    this.baseBox = baseBox;
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  public void setBase(Value base) {
    baseBox.setValue(base);
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<>();
    if (argBoxes != null) {
      Collections.addAll(list, argBoxes);
      for (ValueBox element : argBoxes) {
        list.addAll(element.getValue().getUseBoxes());
      }
    }
    list.addAll(baseBox.getValue().getUseBoxes());
    list.add(baseBox);

    return list;
  }

  @Override
  public abstract Object clone();

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseInstanceInvokeExpr(this);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return baseBox.getValue().equivHashCode() * 101 + getMethodSignature().hashCode() * 17;
  }
}
