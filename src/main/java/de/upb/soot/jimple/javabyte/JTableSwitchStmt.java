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

package de.upb.soot.jimple.javabyte;

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.StmtBox;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.List;

public class JTableSwitchStmt extends AbstractSwitchStmt {
  int lowIndex;
  int highIndex;

  // This method is necessary to deal with constructor-must-be-first-ism.
  private static StmtBox[] getTargetBoxesArray(List<? extends Stmt> targets) {
    StmtBox[] targetBoxes = new StmtBox[targets.size()];
    for (int i = 0; i < targetBoxes.length; i++) {
      targetBoxes[i] = Jimple.getInstance().newStmtBox(targets.get(i));
    }
    return targetBoxes;
  }

  @Override
  public Object clone() {
    return new JTableSwitchStmt(Jimple.cloneIfNecessary(getKey()), lowIndex, highIndex, getTargets(), getDefaultTarget());
  }

  public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends Stmt> targets, Stmt defaultTarget) {
    this(Jimple.getInstance().newImmediateBox(key), lowIndex, highIndex, getTargetBoxesArray(targets),
        Jimple.getInstance().newStmtBox(defaultTarget));
  }

  public JTableSwitchStmt(Value key, int lowIndex, int highIndex, List<? extends StmtBox> targets, StmtBox defaultTarget) {
    this(Jimple.getInstance().newImmediateBox(key), lowIndex, highIndex, targets.toArray(new StmtBox[targets.size()]),
        defaultTarget);
  }

  protected JTableSwitchStmt(ValueBox keyBox, int lowIndex, int highIndex, StmtBox[] targetBoxes, StmtBox defaultTargetBox) {
    super(keyBox, defaultTargetBox, targetBoxes);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
          "Error creating tableswitch: lowIndex(" + lowIndex + ") can't be greater than highIndex(" + highIndex + ").");
    }

    this.lowIndex = lowIndex;
    this.highIndex = highIndex;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String endOfLine = " ";

    buffer.append(Jimple.TABLESWITCH + "(" + keyBox.getValue().toString() + ")" + endOfLine);

    buffer.append("{" + endOfLine);

    // In this for-loop, we cannot use "<=" since 'i' would wrap around.
    // The case for "i == highIndex" is handled separately after the loop.
    for (int i = lowIndex; i < highIndex; i++) {
      Stmt target = getTarget(i - lowIndex);
      buffer.append(
          "    " + Jimple.CASE + " " + i + ": " + Jimple.GOTO + " " + (target == this ? "self" : target) + ";" + endOfLine);
    }
    Stmt target = getTarget(highIndex - lowIndex);
    buffer.append("    " + Jimple.CASE + " " + highIndex + ": " + Jimple.GOTO + " " + (target == this ? "self" : target)
        + ";" + endOfLine);

    target = getDefaultTarget();
    buffer.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " " + (target == this ? "self" : target) + ";" + endOfLine);

    buffer.append("}");

    return buffer.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
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

  private void printCaseTarget(StmtPrinter up, int targetIndex) {
    up.literal("    ");
    up.literal(Jimple.CASE);
    up.literal(" ");
    up.literal(Integer.toString(targetIndex));
    up.literal(": ");
    up.literal(Jimple.GOTO);
    up.literal(" ");
    targetBoxes[targetIndex - lowIndex].toString(up);
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

}
