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
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class JLookupSwitchStmt extends AbstractSwitchStmt implements Copyable {
  /**
   * List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
   */
  private final List<IntConstant> lookupValues;

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
      StmtPositionInfo positionInfo) {
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
      StmtPositionInfo positionInfo) {
    this(
        Jimple.newImmediateBox(key),
        lookupValues,
        targets.toArray(new StmtBox[0]),
        defaultTarget,
        positionInfo);
  }

  private JLookupSwitchStmt(
      ValueBox keyBox,
      List<IntConstant> lookupValues,
      StmtBox[] targetBoxes,
      StmtBox defaultTargetBox,
      StmtPositionInfo positionInfo) {
    super(positionInfo, keyBox, defaultTargetBox, targetBoxes);
    this.lookupValues = Collections.unmodifiableList(new ArrayList<>(lookupValues));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder
        .append(Jimple.LOOKUPSWITCH + "(")
        .append(getKey().toString())
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
    getKeyBox().toString(up);
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
      getTargetBox(i).toString(up);
      up.literal(";");
      up.newline();
    }

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

  @Nonnull
  public JLookupSwitchStmt withKey(Value key) {
    return new JLookupSwitchStmt(
        key, lookupValues, getTargets(), getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JLookupSwitchStmt withTargets(List<? extends Stmt> targets) {
    return new JLookupSwitchStmt(
        getKey(), lookupValues, targets, getDefaultTarget(), getPositionInfo());
  }

  @Nonnull
  public JLookupSwitchStmt withDefaultTarget(Stmt defaultTarget) {
    return new JLookupSwitchStmt(
        getKey(), lookupValues, getTargets(), defaultTarget, getPositionInfo());
  }

  @Nonnull
  public JLookupSwitchStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JLookupSwitchStmt(
        getKey(), lookupValues, getTargets(), getDefaultTarget(), positionInfo);
  }
}
