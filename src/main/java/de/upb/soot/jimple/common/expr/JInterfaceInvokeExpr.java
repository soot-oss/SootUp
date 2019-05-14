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

public class JInterfaceInvokeExpr extends AbstractInstanceInvokeExpr {
  /** */
  private static final long serialVersionUID = 7458533916011927970L;

  /**
   * Assigns bootstrapArgs to bsmArgBoxes, an array of type ValueBox. And methodArgs to an array
   * argBoxes.
   */
  public JInterfaceInvokeExpr(Value base, MethodSignature method, List<? extends Value> args) {
    super(Jimple.newLocalBox(base), method, new ValueBox[args.size()]);

    // FIXME: [JMP] Move this into view or somewhere, where `SootClass` and its context are
    // available
    //    // Check that the method's class is resolved enough
    //    // CheckLevel returns without doing anything because we can be not 'done' resolving
    //    Optional<AbstractClass> declaringClass = view.getClass(method.declClassSignature);
    //    if (declaringClass.isPresent()) {
    //      SootClass cls = (SootClass) declaringClass.get();
    //      // now check if the class is valid
    //      if (!cls.isInterface() && !cls.isPhantomClass()) {
    //        throw new RuntimeException("Trying to create interface invoke expression for
    // non-interface type: " + cls
    //            + " Use JVirtualInvokeExpr or JSpecialInvokeExpr instead!");
    //      }
    //    }

    for (int i = 0; i < args.size(); i++) {
      this.argBoxes[i] = Jimple.newImmediateBox(args.get(i));
    }
  }

  @Override
  public Object clone() {
    List<Value> argList = new ArrayList<>(getArgCount());
    for (int i = 0; i < getArgCount(); i++) {
      argList.add(i, Jimple.cloneIfNecessary(getArg(i)));
    }
    return new JInterfaceInvokeExpr(Jimple.cloneIfNecessary(getBase()), methodSignature, argList);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseInterfaceInvokeExpr(this, o);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append(Jimple.INTERFACEINVOKE)
        .append(" ")
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
    up.literal(Jimple.INTERFACEINVOKE);
    up.literal(" ");
    baseBox.toString(up);
    up.literal(".");
    up.methodSignature(methodSignature);
    up.literal("(");
    argBoxesToPrinter(up);
    up.literal(")");
  }
}
