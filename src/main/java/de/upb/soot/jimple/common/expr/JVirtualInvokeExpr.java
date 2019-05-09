/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
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

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import java.util.ArrayList;
import java.util.List;

public class JVirtualInvokeExpr extends AbstractInstanceInvokeExpr {
  /** */
  private static final long serialVersionUID = 8767212132509253058L;

  /** Stores the values of new ImmediateBox to the argBoxes array. */
  public JVirtualInvokeExpr(Value base, MethodSignature method, List<? extends Value> args) {
    super(Jimple.newLocalBox(base), method, new ValueBox[args.size()]);
    for (int i = 0; i < args.size(); i++) {
      this.argBoxes[i] = Jimple.newImmediateBox(args.get(i));
    }
  }

  @Override
  public Object clone() {
    ArrayList<Value> clonedArgs = new ArrayList<>(getArgCount());
    for (int i = 0; i < getArgCount(); i++) {
      clonedArgs.add(i, getArg(i));
    }
    return new JVirtualInvokeExpr(getBase(), methodSignature, clonedArgs);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseVirtualInvokeExpr(this, o);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append(Jimple.VIRTUALINVOKE + " ")
        .append(baseBox.getValue().toString())
        .append(".")
        .append(methodSignature)
        .append("(");
    argBoxesToString(builder);
    builder.append(")");
    return builder.toString();
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.VIRTUALINVOKE);
    up.literal(" ");
    baseBox.toString(up);
    up.literal(".");
    up.methodSignature(methodSignature);
    up.literal("(");
    argBoxesToPrinter(up);
    up.literal(")");
  }
}
