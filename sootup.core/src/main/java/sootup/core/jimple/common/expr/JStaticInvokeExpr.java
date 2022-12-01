package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Christian Brüggemann, Linghui Luo
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
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** An expression that invokes a static method. */
public final class JStaticInvokeExpr extends AbstractInvokeExpr implements Copyable {

  /** Stores the values to the args array. */
  public JStaticInvokeExpr(@Nonnull MethodSignature method, @Nonnull List<Immediate> args) {
    super(method, args.toArray(new Immediate[0]));
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseStaticInvokeExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return getMethodSignature().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(Jimple.STATICINVOKE).append(" ").append(getMethodSignature()).append("(");
    argsToString(builder);
    builder.append(")");
    return builder.toString();
  }

  /** Converts a parameter of type StmtPrinter to a string literal. */
  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.STATICINVOKE);
    up.literal(" ");
    up.methodSignature(getMethodSignature());
    up.literal("(");
    argsToPrinter(up);
    up.literal(")");
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.caseStaticInvokeExpr(this);
  }

  @Nonnull
  public JStaticInvokeExpr withMethodSignature(@Nonnull MethodSignature methodSignature) {
    return new JStaticInvokeExpr(methodSignature, getArgs());
  }

  @Nonnull
  public JStaticInvokeExpr withArgs(@Nonnull List<Immediate> args) {
    return new JStaticInvokeExpr(getMethodSignature(), args);
  }
}
