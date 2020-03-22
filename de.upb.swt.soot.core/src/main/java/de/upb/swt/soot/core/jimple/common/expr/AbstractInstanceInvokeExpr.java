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

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstanceInvokeExpr extends AbstractInvokeExpr {

  private final ValueBox baseBox;

  // new attribute
  private Value base;

  AbstractInstanceInvokeExpr(ValueBox baseBox, MethodSignature methodSig, ValueBox[] argBoxes) {
    super(methodSig, argBoxes);
    this.baseBox = baseBox;
    // new attribute
    this.base = baseBox.getValue();
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<>();
    List<ValueBox> argBoxes = getArgBoxes();
    if (argBoxes != null) {
      list.addAll(argBoxes);
      for (ValueBox element : argBoxes) {
        list.addAll(element.getValue().getUseBoxes());
      }
    }
    list.addAll(baseBox.getValue().getUseBoxes());
    list.add(baseBox);

    return list;
  }

  // new method
  @Override
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>();
    // getArgs in super class must be modified (not yet)
    List<Value> args = getArgs();
    if (args != null) {
      list.addAll(args);
      for (Value arg : args) {
        list.addAll(arg.getUses());
      }
    }
    list.addAll(base.getUses());
    list.add(base);
    return list;
  }

  @Override
  public void accept(Visitor sw) {
    ((ExprVisitor) sw).caseInstanceInvokeExpr(this);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return baseBox.getValue().equivHashCode() * 101 + getMethodSignature().hashCode() * 17;
  }
}
