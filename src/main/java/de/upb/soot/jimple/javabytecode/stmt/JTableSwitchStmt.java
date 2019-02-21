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

package de.upb.soot.jimple.javabytecode.stmt;

import java.util.List;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

public class JTableSwitchStmt extends AbstractSwitchStmt {
  /**
   * 
   */
  private static final long serialVersionUID = -4716844468557152732L;
  // TODO: final -> immutability?
  int lowIndex;
  int highIndex;

  // This methodRef is necessary to deal with constructor-must-be-first-ism.
  private static List<IStmtBox> getTargetBoxes(List<? extends IStmt> targets) {
    return targets.stream().map(Jimple::newStmtBox).collect(Collectors.toList());
  }

  @Override
  public JTableSwitchStmt clone() {
    return new JTableSwitchStmt(Jimple.cloneIfNecessary(getKey()), lowIndex, highIndex, getTargets(), getDefaultTarget(),
        getPositionInfo().clone());
  }

  public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends IStmt> targets, IStmt defaultTarget,
      PositionInfo positionInfo) {
    this(Jimple.newImmediateBox(key), lowIndex, highIndex, getTargetBoxesArray(targets), Jimple.newStmtBox(defaultTarget),
        positionInfo);
  }

  public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends IStmtBox> targets, IStmtBox defaultTarget,
      PositionInfo positionInfo) {
    this(Jimple.newImmediateBox(key), lowIndex, highIndex, targets.toArray(new IStmtBox[targets.size()]), defaultTarget,
        positionInfo);
  }

  protected JTableSwitchStmt(ValueBox keyBox, int lowIndex, int highIndex, IStmtBox[] targetBoxes, IStmtBox defaultTargetBox,
      PositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
          "Error creating tableswitch: lowIndex(" + lowIndex + ") can't be greater than highIndex(" + highIndex + ").");
    }

    this.lowIndex = lowIndex;
    this.highIndex = highIndex;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder.append(Jimple.TABLESWITCH + "(").append(keyBox.getValue().toString()).append(")").append(endOfLine);

    builder.append("{").append(endOfLine);

    // In this for-loop, we cannot use "<=" since 'i' would wrap around.
    // The case for "i == highIndex" is handled separately after the loop.
    for (int i = lowIndex; i < highIndex; i++) {
      IStmt target = getTarget(i - lowIndex);
      builder.append("    " + Jimple.CASE + " ").append(i).append(": ").append(Jimple.GOTO).append(" ")
          .append(target == this ? "self" : target).append(";").append(endOfLine);
    }
    IStmt target = getTarget(highIndex - lowIndex);
    builder.append("    " + Jimple.CASE + " ").append(highIndex).append(": ").append(Jimple.GOTO).append(" ")
        .append(target == this ? "self" : target).append(";").append(endOfLine);

    target = getDefaultTarget();
    builder.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ").append(target == this ? "self" : target).append(";")
        .append(endOfLine);

    builder.append("}");

    return builder.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.TABLESWITCH);
    up.literal("(");
    keyBox.toString(up);
    up.literal(")");
    up.newline();
    up.literal("{");
    up.newline();
    // In this for-loop, we cannot use "<=" since 'i' would wrap around.
    // The case for "i == highIndex" is handled separately after the loop.
    for (int i = lowIndex; i < highIndex; i++) {
      printCaseTarget(up, i);
    }
    printCaseTarget(up, highIndex);

    up.literal("    ");
    up.literal(Jimple.DEFAULT);
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    defaultTargetBox.toString(up);
    up.literal(";");
    up.newline();
    up.literal("}");
  }

  private void printCaseTarget(IStmtPrinter up, int targetIndex) {
    up.literal("    ");
    up.literal(Jimple.CASE);
    up.literal(" ");
    up.literal(Integer.toString(targetIndex));
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    targetBoxes.get(targetIndex - lowIndex).toString(up);
    up.literal(";");
    up.newline();
  }

  public void setLowIndex(int lowIndex) {
    this.lowIndex = lowIndex;
  }

  public void setHighIndex(int highIndex) {
    this.highIndex = highIndex;
  }

  public int getLowIndex() {
    return lowIndex;
  }

  public int getHighIndex() {
    return highIndex;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseTableSwitchStmt(this);
  }

  @Override
  public boolean equivTo(Object o) {
    return JimpleComparator.getInstance().caseTableSwitchStmt(this, o);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseTableSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    int prime = 31;
    int ret = prime * lowIndex;
    ret = prime * ret + highIndex;
    ret = prime * ret + super.equivHashCode();
    return ret;
  }

}
