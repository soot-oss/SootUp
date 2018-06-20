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

public abstract class AbstractStmtVisitor implements IStmtVisitor {
  Object result;

  @Override
  public void caseBreakpointStmt(JBreakpointStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseInvokeStmt(JInvokeStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseAssignStmt(JAssignStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseIdentityStmt(JIdentityStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(JEnterMonitorStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseExitMonitorStmt(JExitMonitorStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseGotoStmt(JGotoStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseIfStmt(JIfStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseLookupSwitchStmt(JLookupSwitchStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseNopStmt(JNopStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseRetStmt(JRetStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseReturnStmt(JReturnStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseReturnVoidStmt(JReturnVoidStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseTableSwitchStmt(JTableSwitchStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseThrowStmt(JThrowStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void defaultCase(Object obj) {
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
