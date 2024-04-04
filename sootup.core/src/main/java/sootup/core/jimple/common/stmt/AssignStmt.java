package sootup.core.jimple.common.stmt;

import sootup.core.jimple.basic.*;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

import javax.annotation.Nonnull;

public interface AssignStmt extends Stmt {

    @Nonnull
    LValue getLeftOp();

    @Nonnull
    Value getRightOp();

    @Nonnull
    AssignStmt withNewDef(@Nonnull Local newLocal);

    @Nonnull
    AssignStmt withVariable(@Nonnull LValue variable);

    @Nonnull
    AssignStmt withRValue(@Nonnull Value rValue);

    @Nonnull
    AssignStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo);
}
