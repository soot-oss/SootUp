package org.sootup.java.codepropertygraph.evaluation.normalizers;

import java.util.*;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JDynamicInvokeExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body.BodyBuilder;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.PrimitiveType;
import sootup.core.views.View;
import sootup.java.core.types.JavaClassType;

public class DynamicInvokeNormalizer implements BodyInterceptor {
  @Override
  public void interceptBody(@Nonnull BodyBuilder builder, @Nonnull View view) {
    for (Stmt stmt : builder.getStmts()) {
      if (!(stmt instanceof JAssignStmt)) continue;

      for (Value use : stmt.getUses()) {
        if (!(use instanceof JDynamicInvokeExpr)) continue;
        Stmt newStmt = stmt.withNewUse(use, getDummyDynamicInvokeExpr());
        assert newStmt != null;
        builder.replaceStmt(stmt, newStmt);
      }
    }
  }

  private JDynamicInvokeExpr getDummyDynamicInvokeExpr() {
    List<Immediate> testParameterList = Collections.singletonList(IntConstant.getInstance(1));
    List<Immediate> args = Collections.emptyList();
    MethodSignature testDynamicMethod =
        new MethodSignature(
            new JavaClassType(
                JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".") + 1),
                new PackageName(
                    JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.substring(
                        0, JDynamicInvokeExpr.INVOKEDYNAMIC_DUMMY_CLASS_NAME.lastIndexOf(".")))),
            "bootstrap$",
            Collections.emptyList(),
            PrimitiveType.getInt());
    return new JDynamicInvokeExpr(testDynamicMethod, args, testDynamicMethod, testParameterList);
  }
}
