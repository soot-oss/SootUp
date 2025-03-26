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

import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.visitor.ReplaceUseStmtVisitor;

public abstract class AbstractStmt implements Stmt {

  protected final StmtPositionInfo positionInfo;

  public AbstractStmt(@NonNull StmtPositionInfo positionInfo) {
    this.positionInfo = positionInfo;
  }

  /**
   * Returns a list of Values used in this Stmt. Note that they are returned in usual evaluation
   * order.
   */
  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.empty();
  }

  /**
   * Returns a list of Values defined in this Stmt. There are languages which allow multiple return
   * types/assignments so we return a List
   */
  @Override
  @NonNull
  public Optional<LValue> getDef() {
    return Optional.empty();
  }

  /** Returns a list of Values, either used or defined or both in this Stmt. */
  @Override
  @NonNull
  public Stream<Value> getUsesAndDefs() {
    Optional<LValue> def = getDef();
    return def.map(lValue -> Stream.concat(getUses(), Stream.of(lValue))).orElseGet(this::getUses);
  }

  /** Returns the amount of unexceptional successors the Stmt needs to have in the StmtGraph. */
  @Override
  public int getExpectedSuccessorCount() {
    return 1;
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
   * @return a new Stmt with newUse or the current Stmt if oldUse was not found/could not be
   *     replaced in the Stmt
   */
  @Override
  public Stmt withNewUse(@NonNull Value oldUse, @NonNull Value newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    try {
      accept(visitor);
    } catch (ClassCastException cce) {
      // new Stmt is not created as the newUse could not be replaced
      return this;
    }
    return visitor.getResult();
  }

  /**
   * Checks if the statement is an invokable statement, this means it either contains an invoke
   * expression or causes a static initializer call
   *
   * @return true if the Object is an instance of {@link InvokableStmt}, otherwise false
   */
  @Override
  public boolean isInvokableStmt() {
    return this instanceof InvokableStmt;
  }

  /**
   * Transforms the statement to an {@link InvokableStmt} if it is possible. if not it will throw an
   * Exception. Before this method is used {@link #isInvokableStmt} should be called to prevent
   * exceptions
   *
   * @return the typecast of this to InvokableStmt
   */
  @Override
  public InvokableStmt asInvokableStmt() {
    return (InvokableStmt) this;
  }
}
