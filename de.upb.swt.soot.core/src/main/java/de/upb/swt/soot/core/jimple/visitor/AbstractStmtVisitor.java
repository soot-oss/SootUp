package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-1999 Etienne Gagnon
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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JGotoStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JInvokeStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;

public abstract class AbstractStmtVisitor implements StmtVisitor {
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
  public void defaultCase(Object obj) {}

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
