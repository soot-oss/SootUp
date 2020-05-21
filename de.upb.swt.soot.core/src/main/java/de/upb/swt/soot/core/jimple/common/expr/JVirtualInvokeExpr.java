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

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

/** An expression that invokes a virtual method. */
public final class JVirtualInvokeExpr extends AbstractInstanceInvokeExpr implements Copyable {

  /** Stores the values of new ImmediateBox to the argBoxes array. */
  public JVirtualInvokeExpr(Value base, MethodSignature method, List<? extends Value> args) {
    super(Jimple.newLocalBox(base), method, ValueBoxUtils.toValueBoxes(args));
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
        .append(getBase().toString())
        .append(".")
        .append(getMethodSignature())
        .append("(");
    argBoxesToString(builder);
    builder.append(")");
    return builder.toString();
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.VIRTUALINVOKE);
    up.literal(" ");
    getBaseBox().toString(up);
    up.literal(".");
    up.methodSignature(getMethodSignature());
    up.literal("(");
    argBoxesToPrinter(up);
    up.literal(")");
  }

  @Nonnull
  public JVirtualInvokeExpr withBase(Value base) {
    return new JVirtualInvokeExpr(base, getMethodSignature(), getArgs());
  }

  @Nonnull
  public JVirtualInvokeExpr withMethodSignature(MethodSignature methodSignature) {
    return new JVirtualInvokeExpr(getBase(), methodSignature, getArgs());
  }

  @Nonnull
  public JVirtualInvokeExpr withArgs(List<? extends Value> args) {
    return new JVirtualInvokeExpr(getBase(), getMethodSignature(), args);
  }
}
