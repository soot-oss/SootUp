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
