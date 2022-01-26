package de.upb.swt.soot.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann, Markus Schmidt
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

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.visitor.Acceptor;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class Stmt implements EquivTo, Acceptor<StmtVisitor>, Copyable {

  protected final StmtPositionInfo positionInfo;

  public Stmt(@Nonnull StmtPositionInfo positionInfo) {
    this.positionInfo = positionInfo;
  }

  /**
   * Returns a list of Values used in this Stmt. Note that they are returned in usual evaluation
   * order.
   */
  @Nonnull
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  /** Returns a list of Values defined in this Stmt. */
  // TODO: [ms] naming/signature its just a single Def..always.
  @Nonnull
  public List<Value> getDefs() {
    return Collections.emptyList();
  }

  /** Returns a list of Values, either used or defined or both in this Stmt. */
  @Nonnull
  public List<Value> getUsesAndDefs() {
    List<Value> uses = getUses();
    List<Value> defs = getDefs();
    if (uses.isEmpty()) {
      return defs;
    } else if (defs.isEmpty()) {
      return uses;
    } else {
      List<Value> values = new ArrayList<>();
      values.addAll(defs);
      values.addAll(uses);
      return values;
    }
  }

  /**
   * Returns true if execution after this statement may continue at the following statement. (e.g.
   * GotoStmt will return false and e.g. IfStmt will return true).
   */
  public abstract boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. The {@link BranchingStmt}'s GotoStmt, JSwitchStmt and IfStmt will return true.
   */
  public abstract boolean branches();

  /** Returns the amount of successors the Stmt needs to have in the StmtGraph. */
  public int getSuccessorCount() {
    return 1;
  }

  public abstract void toString(@Nonnull StmtPrinter up);

  public boolean containsInvokeExpr() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an InvokeExpr (JInvokeStmt; possible in
   * JAssignStmt) check via containsInvokExpr().
   */
  public AbstractInvokeExpr getInvokeExpr() {
    throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
  }

  public boolean containsArrayRef() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an ArrayRef - possible with JAssignStmts.
   * check via containsArrayRef().
   */
  public JArrayRef getArrayRef() {
    throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
  }

  public boolean containsFieldRef() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an FieldRef - possible with JAssignStmts.
   * check via containsFieldRef().
   */
  public JFieldRef getFieldRef() {
    throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
  }

  public StmtPositionInfo getPositionInfo() {
    return positionInfo;
  }
}
