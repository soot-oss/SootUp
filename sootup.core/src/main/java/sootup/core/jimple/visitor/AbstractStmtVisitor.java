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

import javax.annotation.Nonnull;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;

public abstract class AbstractStmtVisitor<V> extends AbstractVisitor<V> implements StmtVisitor {

  @Override
  public void caseBreakpointStmt(@Nonnull JBreakpointStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt<?, ?> stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIdentityStmt(@Nonnull JIdentityStmt<?> stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseGotoStmt(@Nonnull JGotoStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseNopStmt(@Nonnull JNopStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseRetStmt(@Nonnull JRetStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseReturnVoidStmt(@Nonnull JReturnVoidStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    defaultCaseStmt(stmt);
  }

  @Override
  public void defaultCaseStmt(@Nonnull Stmt stmt) {}
}
