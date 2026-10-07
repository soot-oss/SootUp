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

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * {@code StringConcatFactory}: {@code a.toString()} for each object argument {@code a} that is not
 * a {@code String}, declared by the argument's static type.
 */
final class StringConcatDesugarizer implements InvokeDynamicDesugarizer {

  private static final String STRING_CONCAT_FACTORY = "java.lang.invoke.StringConcatFactory";

  @Override
  public boolean applies(@NonNull JDynamicInvokeExpr expr) {
    return InvokeDynamicDesugarizer.bootstrappedBy(expr, STRING_CONCAT_FACTORY);
  }

  @NonNull
  @Override
  public List<Stmt> desugar(
      @NonNull JDynamicInvokeExpr expr, @NonNull StmtPositionInfo pos, @NonNull Context body) {
    List<Stmt> calls = new ArrayList<>();
    Type string = expr.getType();
    for (int i = 0; i < expr.getArgCount(); i++) {
      if (expr.getArg(i) instanceof Local arg
          && expr.getMethodSignature().getParameterType(i) instanceof ClassType type
          && !type.equals(string)) {
        calls.add(body.call(arg, type, "toString", string, List.of(), List.of(), pos));
      }
    }
    return calls;
  }
}
