package sootup.interceptors.invokedynamic;

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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;

/**
 * Lowers one kind of invokedynamic (identified by its bootstrap method) to equivalent plain Jimple,
 * which {@link InvokeDynamicDesugarer} puts in place of the invokedynamic statement.
 */
public interface InvokeDynamicDesugarizer {

  /** Whether this desugarizer handles {@code expr}. */
  boolean applies(@NonNull JDynamicInvokeExpr expr);

  /**
   * Emits code into {@code code} that does what {@code expr} does and stores its value into {@code
   * result}.
   *
   * @param result where the invokedynamic's value goes, {@code null} if it is discarded
   * @return whether {@code expr} could be lowered; if not, the invokedynamic stays
   */
  boolean desugar(
      @NonNull JDynamicInvokeExpr expr, @Nullable LValue result, @NonNull Fragment code);

  /** Whether {@code expr}'s bootstrap method is declared by {@code className}. */
  static boolean bootstrappedBy(@NonNull JDynamicInvokeExpr expr, @NonNull String className) {
    return expr.getBootstrapMethodSignature()
        .getDeclClassType()
        .getFullyQualifiedName()
        .equals(className);
  }
}
