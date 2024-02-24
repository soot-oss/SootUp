package org.sootup.java.codepropertygraph.evaluation.normalizers;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class SpecialInvokeNormalizer implements BodyInterceptor {
  @Override
  public void interceptBody(@Nonnull BodyBuilder builder, @Nonnull View view) {
    for (Stmt stmt : builder.getStmts()) {
      if (stmt instanceof JInvokeStmt) {
        Optional<JSpecialInvokeExpr> oldInvokeExprOpt =
            stmt.getUses().stream()
                .filter(use -> use instanceof JSpecialInvokeExpr)
                .map(use -> (JSpecialInvokeExpr) use)
                .findFirst();

        if (!oldInvokeExprOpt.isPresent()) continue;
        JSpecialInvokeExpr oldInvokeExpr = oldInvokeExprOpt.get();
        JVirtualInvokeExpr newInvokeExpr = createVirtualInvokeFromSpecialInvoke(oldInvokeExpr);
        Stmt newStmt = new JInvokeStmt(newInvokeExpr, stmt.getPositionInfo());
        builder.replaceStmt(stmt, newStmt);
      }

      if (stmt instanceof JAssignStmt) {
        for (Value use : stmt.getUses()) {
          if (!(use instanceof JSpecialInvokeExpr)) continue;

          Stmt newStmt =
              stmt.withNewUse(use, createVirtualInvokeFromSpecialInvoke((JSpecialInvokeExpr) use));
          assert newStmt != null;
          builder.replaceStmt(stmt, newStmt);
        }
      }
    }
  }

  private JVirtualInvokeExpr createVirtualInvokeFromSpecialInvoke(
          JSpecialInvokeExpr specialInvokeExpr) {
    return new JVirtualInvokeExpr(
            specialInvokeExpr.getBase(),
            specialInvokeExpr.getMethodSignature(),
            specialInvokeExpr.getArgs());
  }
}
