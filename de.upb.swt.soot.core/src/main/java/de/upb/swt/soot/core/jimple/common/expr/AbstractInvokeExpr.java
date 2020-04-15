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

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractInvokeExpr implements Expr {

  private final MethodSignature methodSignature;
  private final Value[] args;

  protected AbstractInvokeExpr(MethodSignature method, Value[] args) {
    this.methodSignature = method;
    this.args = args.length == 0 ? null : args;
  }

  public MethodSignature getMethodSignature() {
    return this.methodSignature;
  }

  public Value getArg(int index) {
    return args[index];
  }

  /** Returns a list of arguments. */
  public List<Value> getArgs() {

    return args != null ? Arrays.asList(args) : Collections.emptyList();
  }

  public int getArgCount() {
    return args == null ? 0 : args.length;
  }

  @Override
  public Type getType() {
    return methodSignature.getType();
  }

  @Override
  public List<Value> getUses() {
    if (args == null) {
      return Collections.emptyList();
    }
    List<Value> list = new ArrayList<>();
    Collections.addAll(list, args);
    for (Value arg : args) {
      list.addAll(arg.getUses());
    }
    return list;
  }

  protected void argsToString(StringBuilder builder) {
    if (args != null) {
      final int len = args.length;
      if (0 < len) {
        builder.append(args[0].toString());
        for (int i = 1; i < len; i++) {
          builder.append(", ");
          builder.append(args[i].toString());
        }
      }
    }
  }

  /** not fixed */
  protected void argsToPrinter(StmtPrinter up) {
    if (args != null) {
      final int len = args.length;
      if (0 < len) {
        args[0].toString(up);
        for (int i = 1; i < len; i++) {
          up.literal(", ");
          args[i].toString(up);
        }
      }
    }
  }
}
