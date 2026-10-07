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
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * {@code ObjectMethods} (a record's {@code toString}/{@code hashCode}/{@code equals}): for each
 * object component {@code f} (the {@code REF_GET_FIELD} bootstrap arguments) {@code
 * this.f.toString()}, {@code this.f.hashCode()} or {@code this.f.equals(((R) other).f)}, matching
 * the invokedynamic's name and declared by the component's type.
 */
final class RecordMethodsDesugarizer implements InvokeDynamicDesugarizer {

  private static final String OBJECT_METHODS = "java.lang.runtime.ObjectMethods";

  @Override
  public boolean applies(@NonNull JDynamicInvokeExpr expr) {
    return InvokeDynamicDesugarizer.bootstrappedBy(expr, OBJECT_METHODS);
  }

  @NonNull
  @Override
  public List<Stmt> desugar(
      @NonNull JDynamicInvokeExpr expr, @NonNull StmtPositionInfo pos, @NonNull Context body) {
    String name = expr.getMethodSignature().getName();
    if (expr.getArgCount() == 0 || !(expr.getArg(0) instanceof Local self)) {
      return List.of();
    }
    boolean equals = name.equals("equals");
    if (!equals && !name.equals("toString") && !name.equals("hashCode")) {
      return List.of();
    }
    if (equals && (expr.getArgCount() < 2 || !(expr.getArg(1) instanceof Local))) {
      return List.of();
    }
    List<Stmt> stmts = new ArrayList<>();
    Local other = null;
    for (Immediate arg : expr.getBootstrapArgs()) {
      if (!(arg instanceof MethodHandle handle)
          || handle.getKind() != MethodHandle.Kind.REF_GET_FIELD
          || !(handle.getReferenceSignature() instanceof FieldSignature field)
          || !(field.getType() instanceof ClassType type)) {
        continue;
      }
      Local value = body.newLocal(type);
      stmts.add(Jimple.newAssignStmt(value, Jimple.newInstanceFieldRef(self, field), pos));
      if (!equals) {
        stmts.add(body.call(value, type, name, expr.getType(), List.of(), List.of(), pos));
        continue;
      }
      if (other == null) {
        ClassType record = field.getDeclClassType();
        other = body.newLocal(record);
        stmts.add(Jimple.newAssignStmt(other, Jimple.newCastExpr(expr.getArg(1), record), pos));
      }
      Local otherValue = body.newLocal(type);
      stmts.add(Jimple.newAssignStmt(otherValue, Jimple.newInstanceFieldRef(other, field), pos));
      Type object = expr.getMethodSignature().getParameterType(1);
      stmts.add(
          body.call(value, type, name, expr.getType(), List.of(object), List.of(otherValue), pos));
    }
    return stmts;
  }
}
