package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.javabyte.JBreakpointStmt;
import de.upb.soot.jimple.javabyte.JEnterMonitorStmt;
import de.upb.soot.jimple.javabyte.JExitMonitorStmt;
import de.upb.soot.jimple.javabyte.JLookupSwitchStmt;
import de.upb.soot.jimple.javabyte.JRetStmt;
import de.upb.soot.jimple.javabyte.JTableSwitchStmt;

public interface IStmtVisitor extends IVisitor {
  public abstract void caseBreakpointStmt(JBreakpointStmt stmt);

  public abstract void caseInvokeStmt(JInvokeStmt stmt);

  public abstract void caseAssignStmt(JAssignStmt stmt);

  public abstract void caseIdentityStmt(JIdentityStmt stmt);

  public abstract void caseEnterMonitorStmt(JEnterMonitorStmt stmt);

  public abstract void caseExitMonitorStmt(JExitMonitorStmt stmt);

  public abstract void caseGotoStmt(JGotoStmt stmt);

  public abstract void caseIfStmt(JIfStmt stmt);

  public abstract void caseLookupSwitchStmt(JLookupSwitchStmt stmt);

  public abstract void caseNopStmt(JNopStmt stmt);

  public abstract void caseRetStmt(JRetStmt stmt);

  public abstract void caseReturnStmt(JReturnStmt stmt);

  public abstract void caseReturnVoidStmt(JReturnVoidStmt stmt);

  public abstract void caseTableSwitchStmt(JTableSwitchStmt stmt);

  public abstract void caseThrowStmt(JThrowStmt stmt);

  public abstract void defaultCase(Object obj);
}
