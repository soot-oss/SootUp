package sootup.core.jimple.common.stmt;

import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.EquivTo;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

public interface Stmt extends EquivTo, Acceptor<StmtVisitor>, Copyable {
  @Nonnull
  List<Value> getUses();

  @Nonnull
  List<LValue> getDefs();

  @Nonnull
  List<Value> getUsesAndDefs();

  /**
   * Returns true if execution after this statement may continue at the following statement. (e.g.
   * GotoStmt will return false and e.g. IfStmt will return true).
   */
  boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. The {@link BranchingStmt}'s GotoStmt, JSwitchStmt and IfStmt will return true.
   */
  boolean branches();

  int getExpectedSuccessorCount();

  void toString(@Nonnull StmtPrinter up);

  boolean containsInvokeExpr();

  AbstractInvokeExpr getInvokeExpr();

  boolean containsArrayRef();

  JArrayRef getArrayRef();

  boolean containsFieldRef();

  JFieldRef getFieldRef();

  StmtPositionInfo getPositionInfo();

  @Nonnull
  Stmt withNewUse(@Nonnull Value oldUse, @Nonnull Value newUse);
}
