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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.visitor.StmtVisitor;
import sootup.core.model.Body;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/**
 * If the condition is true, jumps to the target, otherwise continues to the next stmt. The first
 * successor (index=0) is the fallsthrough Stmt and the second successor (index=1) is the rbanching
 * one.
 */
public final class JIfStmt extends BranchingStmt implements Copyable {

  @Nonnull private final AbstractConditionExpr condition;

  public JIfStmt(@Nonnull AbstractConditionExpr condition, @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.condition = condition;
  }

  @Override
  public String toString() {
    return Jimple.IF + " " + getCondition();
  }

  @Override
  public void toString(@Nonnull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.IF);
    stmtPrinter.literal(" ");
    condition.toString(stmtPrinter);

    stmtPrinter.literal(" ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    // [ms] bounds are validated in Body
    stmtPrinter.stmtRef(stmtPrinter.getGraph().getBranchTargetsOf(this).get(0), true);
  }

  @Nonnull
  public AbstractConditionExpr getCondition() {
    return condition;
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(@Nonnull Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getExpectedSuccessorCount() {
    return 2;
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(getCondition().getUses());
    list.add(getCondition());
    return list;
  }

  @Override
  public void accept(@Nonnull StmtVisitor sw) {
    sw.caseIfStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseIfStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return getCondition().equivHashCode();
  }

  @Nonnull
  public JIfStmt withCondition(@Nonnull AbstractConditionExpr condition) {
    return new JIfStmt(condition, getPositionInfo());
  }

  @Nonnull
  public JIfStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JIfStmt(getCondition(), positionInfo);
  }
}
