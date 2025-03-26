package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, , Linghui Luo, Markus Schmidt and others
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

import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.model.Body;
import sootup.core.util.printer.StmtPrinter;

/**
 * If the condition is true, jumps to the target, otherwise continues to the next stmt. The first
 * successor (index=0) is the fallsthrough Stmt and the second successor (index=1) is the rbanching
 * one.
 */
public final class JIfStmt extends AbstractStmt implements BranchingStmt, FallsThroughStmt {

  public static final int FALSE_BRANCH_IDX = 0;
  public static final int TRUE_BRANCH_IDX = 1;

  @NonNull private final AbstractConditionExpr condition;

  public JIfStmt(@NonNull AbstractConditionExpr condition, @NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.condition = condition;
  }

  @Override
  public String toString() {
    return Jimple.IF + " " + getCondition();
  }

  @Override
  public void toString(@NonNull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.IF);
    stmtPrinter.literal(" ");
    condition.toString(stmtPrinter);

    stmtPrinter.literal(" ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    // [ms] bounds are validated in Body
    stmtPrinter.stmtRef(stmtPrinter.getGraph().getBranchTargetsOf(this).get(0), true);
  }

  @NonNull
  public AbstractConditionExpr getCondition() {
    return condition;
  }

  @Override
  @NonNull
  public List<Stmt> getTargetStmts(@NonNull Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getExpectedSuccessorCount() {
    return 2;
  }

  @Override
  @NonNull
  public Stream<Value> getUses() {
    return Stream.concat(getCondition().getUses(), Stream.of(getCondition()));
  }

  @Override
  public <V extends StmtVisitor> V accept(@NonNull V v) {
    v.caseIfStmt(this);
    return v;
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return true;
  }

  @Override
  public boolean equivTo(@NonNull Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseIfStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getCondition().equivHashCode();
  }

  @NonNull
  public JIfStmt withCondition(@NonNull AbstractConditionExpr condition) {
    return new JIfStmt(condition, getPositionInfo());
  }

  @NonNull
  public JIfStmt withPositionInfo(@NonNull StmtPositionInfo positionInfo) {
    return new JIfStmt(getCondition(), positionInfo);
  }
}
