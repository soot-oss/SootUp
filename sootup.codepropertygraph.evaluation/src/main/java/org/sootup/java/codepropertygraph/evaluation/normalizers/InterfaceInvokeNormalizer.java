package org.sootup.java.codepropertygraph.evaluation.normalizers;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class InterfaceInvokeNormalizer implements BodyInterceptor {
  @Override
  public void interceptBody(@Nonnull BodyBuilder builder, @Nonnull View view) {
    for (Stmt stmt : builder.getStmts()) {

      if (stmt instanceof JInvokeStmt) {
        Optional<JInterfaceInvokeExpr> oldInvokeExprOpt =
            stmt.getUses().stream()
                .filter(use -> use instanceof JInterfaceInvokeExpr)
                .map(use -> (JInterfaceInvokeExpr) use)
                .findFirst();

        if (!oldInvokeExprOpt.isPresent()) continue;
        JInterfaceInvokeExpr oldInvokeExpr = oldInvokeExprOpt.get();
        JVirtualInvokeExpr newInvokeExpr = createVirtualInvokeFromInterfaceInvoke(oldInvokeExpr);
        Stmt newStmt = new JInvokeStmt(newInvokeExpr, stmt.getPositionInfo());
        builder.replaceStmt(stmt, newStmt);
      }

      if (stmt instanceof JAssignStmt) {
        for (Value use : stmt.getUses()) {
          if (!(use instanceof JInterfaceInvokeExpr)) continue;

          Stmt newStmt =
              stmt.withNewUse(
                  use, createVirtualInvokeFromInterfaceInvoke((JInterfaceInvokeExpr) use));
          assert newStmt != null;
          builder.replaceStmt(stmt, newStmt);
        }
      }
    }
  }

  private JVirtualInvokeExpr createVirtualInvokeFromInterfaceInvoke(
      JInterfaceInvokeExpr interfaceInvokeExpr) {
    return new JVirtualInvokeExpr(
        interfaceInvokeExpr.getBase(),
        interfaceInvokeExpr.getMethodSignature(),
        interfaceInvokeExpr.getArgs());
  }
}
