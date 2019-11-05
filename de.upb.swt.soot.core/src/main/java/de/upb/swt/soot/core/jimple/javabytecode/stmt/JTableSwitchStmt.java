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

package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

public final class JTableSwitchStmt extends AbstractSwitchStmt implements Copyable {

  private final int lowIndex;
  private final int highIndex;

  // This method is necessary to deal with constructor-must-be-first-ism.
  private static StmtBox[] getTargetBoxesArray(List<? extends Stmt> targets) {
    StmtBox[] targetBoxes = new StmtBox[targets.size()];
    for (int i = 0; i < targetBoxes.length; i++) {
      targetBoxes[i] = Jimple.newStmtBox(targets.get(i));
    }
    return targetBoxes;
  }

  public JTableSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lowIndex,
        highIndex,
        getTargetBoxesArray(targets),
        Jimple.newStmtBox(defaultTarget),
        positionInfo);
  }

  public JTableSwitchStmt(
      Value key,
      int lowIndex,
      int highIndex,
      List<? extends StmtBox> targets,
      StmtBox defaultTarget,
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lowIndex,
        highIndex,
        targets.toArray(new StmtBox[0]),
        defaultTarget,
        positionInfo);
  }

  private JTableSwitchStmt(
      ValueBox keyBox,
      int lowIndex,
      int highIndex,
      StmtBox[] targetBoxes,
      StmtBox defaultTargetBox,
      StmtPositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);

    if (lowIndex > highIndex) {
      throw new RuntimeException(
          "Error creating tableswitch: lowIndex("
              + lowIndex
              + ") can't be greater than highIndex("
              + highIndex
              + ").");
    }

    this.lowIndex = lowIndex;
    this.highIndex = highIndex;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder
        .append(Jimple.TABLESWITCH + "(")
        .append(getKey().toString())
        .append(")")
        .append(endOfLine);

    builder.append("{").append(endOfLine);

    // In this for-loop, we cannot use "<=" since 'i' would wrap around.
    // The case for "i == highIndex" is handled separately after the loop.
    for (int i = lowIndex; i < highIndex; i++) {
      Stmt target = getTarget(i - lowIndex);
      builder
          .append("    " + Jimple.CASE + " ")
          .append(i)
          .append(": ")
          .append(Jimple.GOTO)
          .append(" ")
          .append(target == this ? "self" : target)
          .append(";")
          .append(endOfLine);
    }
    Stmt target = getTarget(highIndex - lowIndex);
    builder
        .append("    " + Jimple.CASE + " ")
        .append(highIndex)
        .append(": ")
        .append(Jimple.GOTO)
        .append(" ")
        .append(target == this ? "self" : target)
        .append(";")
        .append(endOfLine);

    target = getDefaultTarget();
    builder
        .append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ")
        .append(target == this ? "self" : target)
        .append(";")
        .append(endOfLine);

    builder.append("}");

    return builder.toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.TABLESWITCH);
    up.literal("(");
    getKeyBox().toString(up);
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
    getDefaultTargetBox().toString(up);
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
    getTargetBox(targetIndex - lowIndex).toString(up);
    up.literal(";");
    up.newline();
  }

  public int getLowIndex() {
    return lowIndex;
  }

  public int getHighIndex() {
    return highIndex;
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseTableSwitchStmt(this);
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

  @Nonnull
  public JTableSwitchStmt withKey(Value key) {
    return new JTableSwitchStmt(
        key, lowIndex, highIndex, getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JTableSwitchStmt withLowIndex(int lowIndex) {
    return new JTableSwitchStmt(
        getKey(), lowIndex, highIndex, getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JTableSwitchStmt withHighIndex(int highIndex) {
    return new JTableSwitchStmt(
        getKey(), lowIndex, highIndex, getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JTableSwitchStmt withTargets(List<? extends Stmt> targets) {
    return new JTableSwitchStmt(
        getKey(), lowIndex, highIndex, targets, getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JTableSwitchStmt withDefaultTarget(Stmt defaultTarget) {
    return new JTableSwitchStmt(
        getKey(), lowIndex, highIndex, getTargets(), defaultTarget, getPositionInfo());
  }

  @Nonnull
  public JTableSwitchStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JTableSwitchStmt(
        getKey(), lowIndex, highIndex, getTargets(), getDefaultTarget(), positionInfo);
  }
}
