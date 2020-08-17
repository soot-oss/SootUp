package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Zun Wang and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.signatures.MethodSignature;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInstanceInvokeExpr extends AbstractInvokeExpr {

  private final ValueBox baseBox;

  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private Value base;

  AbstractInstanceInvokeExpr(ValueBox baseBox, MethodSignature methodSig, ValueBox[] argBoxes) {
    super(methodSig, argBoxes);
    this.baseBox = baseBox;
    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.base = baseBox.getValue();
  }

  public Value getBase() {
    return baseBox.getValue();
  }

  public ValueBox getBaseBox() {
    return baseBox;
  }

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

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return baseBox.getValue().equivHashCode() * 101 + getMethodSignature().hashCode() * 17;
  }
}
