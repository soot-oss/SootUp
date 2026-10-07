package sootup.callgraph.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Makes the calls an invokedynamic bootstrap performs implicitly explicit: statements inserted in
 * front of the invokedynamic, which stays. Applied by {@link DynamicInvokeResolver#desugar}.
 */
public interface InvokeDynamicDesugarizer {

  /** Whether this desugarizer handles {@code expr}. */
  boolean applies(@NonNull JDynamicInvokeExpr expr);

  /**
   * The statements to insert in front of the invokedynamic {@code expr}; empty if none.
   *
   * @param pos position of the invokedynamic statement, for the new statements
   * @param body creates locals and calls in the body being rewritten
   */
  @NonNull List<Stmt> desugar(
      @NonNull JDynamicInvokeExpr expr, @NonNull StmtPositionInfo pos, @NonNull Context body);

  /** Whether {@code expr}'s bootstrap method is declared by {@code className}. */
  static boolean bootstrappedBy(@NonNull JDynamicInvokeExpr expr, @NonNull String className) {
    return expr.getBootstrapMethodSignature()
        .getDeclClassType()
        .getFullyQualifiedName()
        .equals(className);
  }

  /** The body being rewritten. */
  interface Context {

    /** A fresh local of {@code type}, added to the body. */
    @NonNull Local newLocal(@NonNull Type type);

    /**
     * {@code base.name(args)} declared by {@code declaringType} - an interface or virtual invoke,
     * matching the declaring type.
     */
    @NonNull Stmt call(
        @NonNull Local base,
        @NonNull ClassType declaringType,
        @NonNull String name,
        @NonNull Type returnType,
        @NonNull List<Type> parameterTypes,
        @NonNull List<Immediate> args,
        @NonNull StmtPositionInfo pos);
  }
}
