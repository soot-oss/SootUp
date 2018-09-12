/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Etienne Gagnon
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

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
import de.upb.soot.jimple.javabytecode.stmt.JBreakpointStmt;
import de.upb.soot.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.soot.jimple.javabytecode.stmt.JRetStmt;
import de.upb.soot.jimple.javabytecode.stmt.JTableSwitchStmt;

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
