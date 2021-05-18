package de.upb.swt.soot.core.jimple.visitor;

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

import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;

public abstract class AbstractStmtVisitor<V> extends AbstractVisitor<V> implements StmtVisitor {

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
  public void caseSwitchStmt(JSwitchStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void caseThrowStmt(JThrowStmt stmt) {
    defaultCase(stmt);
  }

  @Override
  public void defaultCase(Stmt stmt) {}
}
