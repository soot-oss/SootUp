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

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;

/**
 * {@code StringConcatFactory.makeConcat[WithConstants]} lowered to a {@code StringBuilder} chain,
 * as javac 8 compiles string concatenation. Arguments are rendered as {@code String.valueOf} does,
 * by their static type (see {@link StringBuilding}).
 */
public final class StringConcatDesugarizer implements InvokeDynamicDesugarizer {

  private static final String STRING_CONCAT_FACTORY = "java.lang.invoke.StringConcatFactory";
  private static final char ARG_TAG = '\u0001';
  private static final char CONST_TAG = '\u0002';

  @Override
  public boolean applies(@NonNull JDynamicInvokeExpr expr) {
    String name = expr.getBootstrapMethodSignature().getName();
    return InvokeDynamicDesugarizer.bootstrappedBy(expr, STRING_CONCAT_FACTORY)
        && (name.equals("makeConcat") || name.equals("makeConcatWithConstants"));
  }

  @Override
  public boolean desugar(
      @NonNull JDynamicInvokeExpr expr, @Nullable LValue result, @NonNull Fragment code) {
    List<Immediate> bootstrapArgs = expr.getBootstrapArgs();
    boolean withConstants = expr.getBootstrapMethodSignature().getName().endsWith("WithConstants");
    if (withConstants
        && (bootstrapArgs.isEmpty() || !(bootstrapArgs.get(0) instanceof StringConstant))) {
      return false;
    }
    StringBuilding string = new StringBuilding(code);
    if (!withConstants) {
      for (int i = 0; i < expr.getArgCount(); i++) {
        string.appendValue(expr.getArg(i), expr.getMethodSignature().getParameterType(i));
      }
      string.finish(result);
      return true;
    }
    String recipe = ((StringConstant) bootstrapArgs.get(0)).getValue();
    StringBuilder text = new StringBuilder();
    int arg = 0;
    int constant = 1;
    for (char c : recipe.toCharArray()) {
      if (c != ARG_TAG && c != CONST_TAG) {
        text.append(c);
        continue;
      }
      string.appendText(text.toString());
      text.setLength(0);
      if (c == ARG_TAG) {
        string.appendValue(expr.getArg(arg), expr.getMethodSignature().getParameterType(arg));
        arg++;
      } else {
        Immediate value = bootstrapArgs.get(constant++);
        string.appendValue(value, value.getType());
      }
    }
    string.appendText(text.toString());
    string.finish(result);
    return true;
  }
}
