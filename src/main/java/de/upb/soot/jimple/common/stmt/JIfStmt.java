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

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.$StmtBoxAccessor;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JIfStmt extends AbstractStmt {
  /** */
  private static final long serialVersionUID = -5625186075843518011L;

  final ValueBox conditionBox;
  final IStmtBox targetBox;

  final List<IStmtBox> targetBoxes;

  public JIfStmt(Value condition, IStmt target, PositionInfo positionInfo) {
    this(condition, Jimple.newStmtBox(target), positionInfo);
  }

  public JIfStmt(Value condition, IStmtBox target, PositionInfo positionInfo) {
    this(Jimple.newConditionExprBox(condition), target, positionInfo);
  }

  protected JIfStmt(ValueBox conditionBox, IStmtBox targetBox, PositionInfo positionInfo) {
    super(positionInfo);
    this.conditionBox = conditionBox;
    this.targetBox = targetBox;

    targetBoxes = Collections.singletonList(targetBox);
  }

  @Override
  public JIfStmt clone() {
    return new JIfStmt(
        Jimple.cloneIfNecessary(getCondition()), getTarget(), getPositionInfo().clone());
  }

  @Override
  public String toString() {
    IStmt t = getTarget();
    String target = "(branch)";
    if (!t.branches()) {
      target = t.toString();
    }
    return Jimple.IF + " " + getCondition().toString() + " " + Jimple.GOTO + " " + target;
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.IF);
    up.literal(" ");
    conditionBox.toString(up);
    up.literal(" ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    targetBox.toString(up);
  }

  public Value getCondition() {
    return conditionBox.getValue();
  }

  public ValueBox getConditionBox() {
    return conditionBox;
  }

  public IStmt getTarget() {
    return targetBox.getStmt();
  }

  void setTarget(IStmt target) {
    $StmtBoxAccessor.setStmt(targetBox, target);
  }

  public IStmtBox getTargetBox() {
    return targetBox;
  }

  @Override
  public List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<>(conditionBox.getValue().getUseBoxes());
    useBoxes.add(conditionBox);

    return useBoxes;
  }

  @Override
  public final List<IStmtBox> getStmtBoxes() {
    return targetBoxes;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseIfStmt(this);
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
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseIfStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return conditionBox.getValue().equivHashCode() + 31 * targetBox.getStmt().equivHashCode();
  }
}
