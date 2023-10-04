package sootup.core.jimple.common.stmt;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.visitor.ReplaceUseStmtVisitor;

public abstract class AbstractStmt implements Stmt {

  protected final StmtPositionInfo positionInfo;

  public AbstractStmt(@Nonnull StmtPositionInfo positionInfo) {
    this.positionInfo = positionInfo;
  }

  /**
   * Returns a list of Values used in this Stmt. Note that they are returned in usual evaluation
   * order.
   */
  @Override
  @Nonnull
  public List<Value> getUses() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Values defined in this Stmt. There are languages which allow multiple return
   * types/assignments so we return a List
   */
  @Override
  @Nonnull
  public List<LValue> getDefs() {
    return Collections.emptyList();
  }

  /** Returns a list of Values, either used or defined or both in this Stmt. */
  @Override
  @Nonnull
  public List<Value> getUsesAndDefs() {
    List<Value> uses = getUses();
    List<Value> defs = new ArrayList<>(getDefs());
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

  /** Returns the amount of unexceptional successors the Stmt needs to have in the StmtGraph. */
  @Override
  public int getExpectedSuccessorCount() {
    return 1;
  }

  @Override
  public boolean containsInvokeExpr() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an InvokeExpr (JInvokeStmt; possible in
   * JAssignStmt) check via containsInvokExpr().
   */
  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
  }

  @Override
  public boolean containsArrayRef() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an ArrayRef - possible with JAssignStmts.
   * check via containsArrayRef().
   */
  @Override
  public JArrayRef getArrayRef() {
    throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
  }

  @Override
  public boolean containsFieldRef() {
    return false;
  }

  /**
   * This method must only be used for Stmts which contain an FieldRef - possible with JAssignStmts.
   * check via containsFieldRef().
   */
  @Override
  public JFieldRef getFieldRef() {
    throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
  }

  @Override
  public StmtPositionInfo getPositionInfo() {
    return positionInfo;
  }

  /**
   * Use newUse to replace the oldUse in oldStmt.
   *
   * @param oldUse a Value in the useList of oldStmt.
   * @param newUse a Value is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Override
  @Nonnull
  public Stmt withNewUse(@Nonnull Value oldUse, @Nonnull Value newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    accept(visitor);
    return visitor.getResult();
  }
}
