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
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** conditional jumps to a target Stmt, otherwise the flow continues to the next Stmt. */
public final class JIfStmt extends Stmt implements Copyable {

  @Nonnull private final AbstractConditionExpr condition;
  @Nonnull private Stmt target = null;

  @Nonnull private List<Stmt> targets;

  public JIfStmt(
      @Nonnull AbstractConditionExpr condition,
      @Nonnull Stmt target,
      @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.condition = condition;
    setTarget(target);
  }

  @Override
  public String toString() {
    Stmt t = getTarget();
    String target = "(branch)";
    if (!t.branches()) {
      target = t.toString();
    }
    return Jimple.IF + " " + getCondition().toString() + " " + Jimple.GOTO + " " + target;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.IF);
    up.literal(" ");
    condition.toString(up);
    up.literal(" ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    up.stmtRef(target, true);
  }

  public Value getCondition() {
    return condition;
  }

  public Stmt getTarget() {
    return target;
  }

  /** Violates immutability. Only use this for legacy code. */
  @Deprecated
  private void setTarget(@Nonnull Stmt target) {
    if (this.target != null) {
      Stmt.$Accessor.removeStmtPointingToThis(this, this.target);
    }
    this.target = target;
    this.targets = Collections.singletonList(target);
    Stmt.$Accessor.addStmtPointingToThis(this, target);
  }

  @Override
  @Nonnull
  public List<Value> getUses() {
    List<Value> list = new ArrayList<>(condition.getUses());
    list.add(condition);
    return list;
  }

  @Override
  @Nonnull
  public final List<Stmt> getStmts() {
    return targets;
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
    return condition.equivHashCode() + 31 * target.equivHashCode();
  }

  @Nonnull
  public JIfStmt withCondition(@Nonnull AbstractConditionExpr condition) {
    return new JIfStmt(condition, getTarget(), getPositionInfo());
  }

  @Nonnull
  public JIfStmt withTarget(@Nonnull Stmt target) {
    return new JIfStmt((AbstractConditionExpr) getCondition(), target, getPositionInfo());
  }

  @Nonnull
  public JIfStmt withPositionInfo(@Nonnull StmtPositionInfo positionInfo) {
    return new JIfStmt((AbstractConditionExpr) getCondition(), getTarget(), positionInfo);
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setTarget(@Nonnull JIfStmt fromStmt, @Nonnull Stmt targetStmt) {
      fromStmt.setTarget(targetStmt);
    }

    private $Accessor() {}
  }
}
