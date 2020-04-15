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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractUnopExpr implements Expr {

  private final Value op;

  AbstractUnopExpr(Value op) {

    if (op == null) {
      throw new IllegalArgumentException("value may not be null");
    }
    if (op instanceof Immediate) {
      this.op = op;
    } else {
      throw new RuntimeException(
          "UnopExpr " + this + " cannot contain value: " + op + " (" + op.getClass() + ")");
    }
  }

  public Value getOp() {
    return op;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(op.getUses());
    list.add(op);

    return list;
  }
}
