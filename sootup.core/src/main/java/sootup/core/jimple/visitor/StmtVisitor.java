package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Christian Brüggemann
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

import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;

public interface StmtVisitor extends Visitor {
  void caseBreakpointStmt(JBreakpointStmt stmt);

  void caseInvokeStmt(JInvokeStmt stmt);

  void caseAssignStmt(JAssignStmt<?, ?> stmt);

  void caseIdentityStmt(JIdentityStmt<?> stmt);

  void caseEnterMonitorStmt(JEnterMonitorStmt stmt);

  void caseExitMonitorStmt(JExitMonitorStmt stmt);

  void caseGotoStmt(JGotoStmt stmt);

  void caseIfStmt(JIfStmt stmt);

  void caseNopStmt(JNopStmt stmt);

  void caseRetStmt(JRetStmt stmt);

  void caseReturnStmt(JReturnStmt stmt);

  void caseReturnVoidStmt(JReturnVoidStmt stmt);

  void caseSwitchStmt(JSwitchStmt stmt);

  void caseThrowStmt(JThrowStmt stmt);

  void defaultCaseStmt(Stmt stmt);
}
