package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Markus Schmidt and others
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

import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.printer.StmtPrinter;

/** An expression that invokes a virtual method. */
public final class JVirtualInvokeExpr extends AbstractInstanceInvokeExpr {

  /** Stores the values to the args array. */
  public JVirtualInvokeExpr(
      @NonNull Local base, @NonNull MethodSignature method, @NonNull List<Immediate> args) {
    super(base, method, args.toArray(new Immediate[0]));
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseVirtualInvokeExpr(this, o);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
        .append(Jimple.VIRTUALINVOKE + " ")
        .append(getBase())
        .append(".")
        .append(getMethodSignature())
        .append("(");
    argsToString(builder);
    builder.append(")");
    return builder.toString();
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.VIRTUALINVOKE);
    up.literal(" ");
    getBase().toString(up);
    up.literal(".");
    up.methodSignature(getMethodSignature());
    up.literal("(");
    argsToPrinter(up);
    up.literal(")");
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseVirtualInvokeExpr(this);
    return v;
  }

  @Override
  @NonNull
  public JVirtualInvokeExpr withBase(@NonNull Local base) {
    return new JVirtualInvokeExpr(base, getMethodSignature(), getArgs());
  }

  @Override
  @NonNull
  public JVirtualInvokeExpr withMethodSignature(@NonNull MethodSignature methodSignature) {
    return new JVirtualInvokeExpr(getBase(), methodSignature, getArgs());
  }

  @Override
  @NonNull
  public JVirtualInvokeExpr withArgs(@NonNull List<Immediate> args) {
    return new JVirtualInvokeExpr(getBase(), getMethodSignature(), args);
  }
}
