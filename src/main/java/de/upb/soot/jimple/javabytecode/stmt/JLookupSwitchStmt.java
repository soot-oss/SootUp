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

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.JimpleComparator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.visitor.StmtVisitor;
import de.upb.soot.jimple.visitor.Visitor;
import de.upb.soot.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JLookupSwitchStmt extends AbstractSwitchStmt {
  /** */
  private static final long serialVersionUID = 7072376393810033195L;
  /**
   * List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
   */
  List<IntConstant> lookupValues;

  // This method is necessary to deal with constructor-must-be-first-ism.
  private static StmtBox[] getTargetBoxesArray(List<? extends Stmt> targets) {
    StmtBox[] targetBoxes = new StmtBox[targets.size()];
    for (int i = 0; i < targetBoxes.length; i++) {
      targetBoxes[i] = Jimple.newStmtBox(targets.get(i));
    }
    return targetBoxes;
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(
      Value key,
      List<IntConstant> lookupValues,
      List<? extends Stmt> targets,
      Stmt defaultTarget,
      PositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lookupValues,
        getTargetBoxesArray(targets),
        Jimple.newStmtBox(defaultTarget),
        positionInfo);
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(
      Value key,
      List<IntConstant> lookupValues,
      List<? extends StmtBox> targets,
      StmtBox defaultTarget,
      PositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lookupValues,
        targets.toArray(new StmtBox[0]),
        defaultTarget,
        positionInfo);
  }

  protected JLookupSwitchStmt(
      ValueBox keyBox,
      List<IntConstant> lookupValues,
      StmtBox[] targetBoxes,
      StmtBox defaultTargetBox,
      PositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);
    setLookupValues(lookupValues);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder
        .append(Jimple.LOOKUPSWITCH + "(")
        .append(keyBox.getValue().toString())
        .append(")")
        .append(endOfLine);

    builder.append("{").append(endOfLine);

    for (int i = 0; i < lookupValues.size(); i++) {
      Stmt target = getTarget(i);
      builder
          .append("    " + Jimple.CASE + " ")
          .append(lookupValues.get(i))
          .append(": ")
          .append(Jimple.GOTO)
          .append(" ")
          .append(target == this ? "self" : target)
          .append(";")
          .append(endOfLine);
    }

    Stmt target = getDefaultTarget();
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
    up.literal(Jimple.LOOKUPSWITCH);
    up.literal("(");
    keyBox.toString(up);
    up.literal(")");
    up.newline();
    up.literal("{");
    up.newline();
    final int size = lookupValues.size();
    for (int i = 0; i < size; i++) {
      up.literal("    ");
      up.literal(Jimple.CASE);
      up.literal(" ");
      up.constant(lookupValues.get(i));
      up.literal(": ");
      up.literal(Jimple.GOTO);
      up.literal(" ");
      targetBoxes[i].toString(up);
      up.literal(";");
      up.newline();
    }

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

  public void setLookupValues(List<IntConstant> lookupValues) {
    this.lookupValues = new ArrayList<>(lookupValues);
  }

  public void setLookupValue(int index, int value) {
    lookupValues.set(index, IntConstant.getInstance(value));
  }

  public int getLookupValueCount() {
    return lookupValues.size();
  }

  public int getLookupValue(int index) {
    return lookupValues.get(index).getValue();
  }

  public List<IntConstant> getLookupValues() {
    return Collections.unmodifiableList(lookupValues);
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseLookupSwitchStmt(this);
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseLookupSwitchStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    int res = super.equivHashCode();
    int prime = 31;

    for (IntConstant lv : lookupValues) {
      res = res * prime + lv.equivHashCode();
    }

    return res;
  }
}
