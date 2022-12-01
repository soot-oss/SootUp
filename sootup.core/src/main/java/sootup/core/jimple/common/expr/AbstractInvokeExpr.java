package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Value;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

public abstract class AbstractInvokeExpr implements Expr {

  @Nonnull private final MethodSignature methodSignature;
  @Nonnull private final Immediate[] args;

  protected AbstractInvokeExpr(@Nonnull MethodSignature method, @Nonnull Immediate[] args) {
    this.methodSignature = method;
    for (Immediate arg : args) {
      if (arg == null) {
        throw new IllegalArgumentException("arg may not be null");
      }
    }
    this.args = args;
  }

  @Nonnull
  public MethodSignature getMethodSignature() {
    return this.methodSignature;
  }

  public Value getArg(int index) {
    return args[index];
  }

  /** Returns a list of arguments. */
  public List<Immediate> getArgs() {
    return Collections.unmodifiableList(Arrays.asList(args));
  }

  public int getArgCount() {
    return args.length;
  }

  @Nonnull
  @Override
  public Type getType() {
    return methodSignature.getType();
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(getArgCount());
    Collections.addAll(list, args);
    for (Value arg : args) {
      list.addAll(arg.getUses());
    }
    return list;
  }

  protected void argsToString(@Nonnull StringBuilder builder) {
    final int len = getArgCount();
    if (0 < len) {
      builder.append(args[0].toString());
      for (int i = 1; i < len; i++) {
        builder.append(", ");
        builder.append(args[i].toString());
      }
    }
  }

  protected void argsToPrinter(@Nonnull StmtPrinter up) {
    final int len = getArgCount();
    if (0 < len) {
      args[0].toString(up);
      for (int i = 1; i < len; i++) {
        up.literal(", ");
        args[i].toString(up);
      }
    }
  }
}
