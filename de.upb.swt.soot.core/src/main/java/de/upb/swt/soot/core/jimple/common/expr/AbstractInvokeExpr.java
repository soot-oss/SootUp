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

import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class AbstractInvokeExpr implements Expr {

  @Nonnull private final MethodSignature methodSignature;
  @Nonnull private final List<Immediate> args;

  protected AbstractInvokeExpr(@Nonnull MethodSignature method, @Nonnull List<Immediate> args) {
    this.methodSignature = method;
    for (Immediate arg : args) {
      if (arg == null) {
        throw new IllegalArgumentException("arg may not be null");
      }
    }
    this.args = ImmutableUtils.immutableListOf(args);
  }

  public MethodSignature getMethodSignature() {
    return this.methodSignature;
  }

  public Immediate getArg(@Nonnull int index) {
    return args.get(index);
  }

  /** Returns a list of arguments. */
  public List<Immediate> getArgs() {
    return args;
  }

  public int getArgCount() {
    return args.size();
  }

  @Override
  public Type getType() {
    return methodSignature.getType();
  }

  @Override
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(args.size());
    list.addAll(args);
    for (Immediate arg : args) {
      list.addAll(arg.getUses());
    }
    return list;
  }

  protected void argsToString(@Nonnull StringBuilder builder) {
    final int len = args.size();
    if (0 < len) {
      builder.append(args.get(0).toString());
      for (int i = 1; i < len; i++) {
        builder.append(", ");
        builder.append(args.get(i).toString());
      }
    }
  }

  protected void argsToPrinter(@Nonnull StmtPrinter up) {
    final int len = args.size();
    if (0 < len) {
      args.get(0).toString(up);
      for (int i = 1; i < len; i++) {
        up.literal(", ");
        args.get(i).toString(up);
      }
    }
  }
}
