package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo and others
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;

public abstract class AbstractStmtVisitor implements StmtVisitor, Visitor {

  @Override
  public void caseBreakpointStmt(@NonNull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@NonNull JInvokeStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseAssignStmt(@NonNull JAssignStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIdentityStmt(@NonNull JIdentityStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@NonNull JEnterMonitorStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseExitMonitorStmt(@NonNull JExitMonitorStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseGotoStmt(@NonNull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@NonNull JIfStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseNopStmt(@NonNull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@NonNull JRetStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnStmt(@NonNull JReturnStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnVoidStmt(@NonNull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@NonNull JSwitchStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseThrowStmt(@NonNull JThrowStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void defaultCaseStmt(@NonNull Stmt stmt) {}
}
