package de.upb.swt.soot.core.jimple.common.stmt;

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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

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
    /*  // TODO [ms] leftover: Stmt t = getTarget();
       String target = "(branch)";
       if (!t.branches()) {
         target = t.toString();
       }
    */
    return Jimple.IF
        + " "
        + getCondition().toString(); // TODO [ms] leftover: + " " + Jimple.GOTO + " " + target;
  }

  @Override
  public void toString(@Nonnull StmtPrinter stmtPrinter) {
    stmtPrinter.literal(Jimple.IF);
    stmtPrinter.literal(" ");
    condition.toString(stmtPrinter);

    stmtPrinter.literal(" ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    stmtPrinter.stmtRef(getTarget(stmtPrinter.getBody()), true);
  }

  public AbstractConditionExpr getCondition() {
    return condition;
  }

  public Stmt getTarget(Body body) {
    // [ms] bounds are validated in Body
    return getTargetStmts(body).get(1);
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  public int getSuccessorCount() {
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
  public boolean branches() {
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
