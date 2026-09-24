package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2023 Linghui Luo, Christian Brüggemann, Markus Schmidt
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
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.EquivTo;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.javabytecode.stmt.*;
import sootup.core.jimple.visitor.Acceptor;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.util.printer.StmtPrinter;

public interface Stmt extends EquivTo, Acceptor<StmtVisitor> {
  /** Collects all values used by this statement into the given list. */
  void collectUses(List<Value> collector);

  @NonNull
  default List<Value> getUses() {
    ArrayList<Value> collector = new ArrayList<>();
    collectUses(collector);
    return collector;
  }

  /** Returns the value defined (written) by this statement, if any. */
  @NonNull Optional<LValue> getDef();

  @NonNull
  default List<Value> getUsesAndDefs() {
    ArrayList<Value> collector = new ArrayList<>();
    collectUses(collector);
    getDef().ifPresent(collector::add);
    return collector;
  }

  /**
   * Returns true if execution after this statement may continue at the following statement. (e.g.
   * GotoStmt will return false and e.g. IfStmt will return true).
   *
   * @return true if control may fall through to the next statement
   */
  boolean fallsThrough();

  /**
   * Returns true if execution after this statement does not necessarily continue at the following
   * statement. The {@link BranchingStmt}'s GotoStmt, JSwitchStmt and IfStmt will return true.
   *
   * @return true if this statement may branch to a non-sequential successor
   */
  boolean branches();

  int getExpectedSuccessorCount();

  void toString(@NonNull StmtPrinter up);

  boolean containsArrayRef();

  JArrayRef getArrayRef();

  boolean containsFieldRef();

  JFieldRef getFieldRef();

  StmtPositionInfo getPositionInfo();

  Stmt withNewUse(@NonNull Value oldUse, @NonNull Value newUse);

  boolean isInvokableStmt();

  InvokableStmt asInvokableStmt();

  default boolean isJAssignStmt() {
    return false;
  }

  default boolean isJBreakpointStmt() {
    return false;
  }

  default boolean isJEnterMonitorStmt() {
    return false;
  }

  default boolean isJExitMonitorStmt() {
    return false;
  }

  default boolean isJGotoStmt() {
    return false;
  }

  default boolean isJIdentityStmt() {
    return false;
  }

  default boolean isJIfStmt() {
    return false;
  }

  default boolean isJInvokeStmt() {
    return false;
  }

  default boolean isJNopStmt() {
    return false;
  }

  default boolean isJRetStmt() {
    return false;
  }

  default boolean isJReturnStmt() {
    return false;
  }

  default boolean isJReturnVoidStmt() {
    return false;
  }

  default boolean isJSwitchStmt() {
    return false;
  }

  default boolean isJThrowStmt() {
    return false;
  }

  default JAssignStmt asJAssignStmt() {
    return null;
  }

  default JBreakpointStmt asJBreakpointStmt() {
    return null;
  }

  default JEnterMonitorStmt asJEnterMonitorStmt() {
    return null;
  }

  default JExitMonitorStmt asJExitMonitorStmt() {
    return null;
  }

  default JGotoStmt asJGotoStmt() {
    return null;
  }

  default JIdentityStmt asJIdentityStmt() {
    return null;
  }

  default JIfStmt asJIfStmt() {
    return null;
  }

  default JInvokeStmt asJInvokeStmt() {
    return null;
  }

  default JNopStmt asJNopStmt() {
    return null;
  }

  default JRetStmt asJRetStmt() {
    return null;
  }

  default JReturnStmt asJReturnStmt() {
    return null;
  }

  default JReturnVoidStmt asJReturnVoidStmt() {
    return null;
  }

  default JSwitchStmt asJSwitchStmt() {
    return null;
  }

  default JThrowStmt asJThrowStmt() {
    return null;
  }

  default Optional<JAssignStmt> toJAssignStmt() {
    return Optional.empty();
  }

  default Optional<JBreakpointStmt> toJBreakpointStmt() {
    return Optional.empty();
  }

  default Optional<JEnterMonitorStmt> toJEnterMonitorStmt() {
    return Optional.empty();
  }

  default Optional<JExitMonitorStmt> toJExitMonitorStmt() {
    return Optional.empty();
  }

  default Optional<JGotoStmt> toJGotoStmt() {
    return Optional.empty();
  }

  default Optional<JIdentityStmt> toJIdentityStmt() {
    return Optional.empty();
  }

  default Optional<JIfStmt> toJIfStmt() {
    return Optional.empty();
  }

  default Optional<JInvokeStmt> toJInvokeStmt() {
    return Optional.empty();
  }

  default Optional<JNopStmt> toJNopStmt() {
    return Optional.empty();
  }

  default Optional<JRetStmt> toJRetStmt() {
    return Optional.empty();
  }

  default Optional<JReturnStmt> toJReturnStmt() {
    return Optional.empty();
  }

  default Optional<JReturnVoidStmt> toJReturnVoidStmt() {
    return Optional.empty();
  }

  default Optional<JSwitchStmt> toJSwitchStmt() {
    return Optional.empty();
  }

  default Optional<JThrowStmt> toJThrowStmt() {
    return Optional.empty();
  }
}
