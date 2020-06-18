/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** If the condition is true, jumps to the target, otherwise continues to the next stmt. */
public final class JIfStmt extends BranchingStmt implements Copyable {

  private final ValueBox conditionBox;

  public JIfStmt(Value condition, StmtPositionInfo positionInfo) {
    this(Jimple.newConditionExprBox(condition), positionInfo);
  }

  private JIfStmt(ValueBox conditionBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.conditionBox = conditionBox;
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
    conditionBox.toString(stmtPrinter);

    stmtPrinter.literal(" ");
    stmtPrinter.literal(Jimple.GOTO);
    stmtPrinter.literal(" ");
    stmtPrinter.stmtRef(stmtPrinter.branchTargets(this).get(1), true);
  }

  public Value getCondition() {
    return conditionBox.getValue();
  }

  public ValueBox getConditionBox() {
    return conditionBox;
  }

  public Stmt getTarget(Body body) {
    // TODO: [ms] validate in builder!
    return getTargetStmts(body).get(1);
  }

  @Override
  @Nonnull
  public List<Stmt> getTargetStmts(Body body) {
    return body.getBranchTargetsOf(this);
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(getCondition().getUses());
    list.add(getCondition());
    return list;
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((StmtVisitor) sw).caseIfStmt(this);
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
    return conditionBox.getValue().equivHashCode();
  }

  @Nonnull
  public JIfStmt withCondition(@Nonnull AbstractConditionExpr condition) {
    return new JIfStmt(condition, getPositionInfo());
  }

  @Nonnull
  public JIfStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JIfStmt((AbstractConditionExpr) getCondition(), positionInfo);
  }
}
