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
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.util.printer.IStmtPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class JLookupSwitchStmt extends AbstractSwitchStmt {
  /**
   * 
   */
  private static final long serialVersionUID = 7072376393810033195L;
  /**
   * List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
   */
  List<IntConstant> lookupValues;

  // This method is necessary to deal with constructor-must-be-first-ism.
  private static List<IStmtBox> getTargetBoxes(List<? extends IStmt> targets) {
    return targets.stream().map(Jimple::newStmtBox).collect(Collectors.toList());
  }

  @Override
  public JLookupSwitchStmt clone() {
    int lookupValueCount = lookupValues.size();
    List<IntConstant> clonedLookupValues = new ArrayList<>(lookupValueCount);

    for (int i = 0; i < lookupValueCount; i++) {
      clonedLookupValues.add(i, IntConstant.getInstance(getLookupValue(i)));
    }

    return new JLookupSwitchStmt(getKey(), clonedLookupValues, getTargets(), getDefaultTarget());
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends IStmt> targets, IStmt defaultTarget) {
    this(Jimple.newImmediateBox(key), lookupValues, getTargetBoxes(targets), Jimple.newStmtBox(defaultTarget));
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends IStmtBox> targets,
      IStmtBox defaultTarget) {
    this(Jimple.newImmediateBox(key), lookupValues, new ArrayList<>(targets), defaultTarget);
  }

  protected JLookupSwitchStmt(ValueBox keyBox, List<IntConstant> lookupValues, List<IStmtBox> targetBoxes,
      IStmtBox defaultTargetBox) {
    super(keyBox, defaultTargetBox, targetBoxes);
    setLookupValues(lookupValues);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    String endOfLine = " ";

    builder.append(Jimple.LOOKUPSWITCH + "(").append(keyBox.getValue().toString()).append(")").append(endOfLine);

    builder.append("{").append(endOfLine);

    for (int i = 0; i < lookupValues.size(); i++) {
      IStmt target = getTarget(i);
      builder.append("    " + Jimple.CASE + " ").append(lookupValues.get(i)).append(": ").append(Jimple.GOTO).append(" ")
          .append(target == this ? "self" : target).append(";").append(endOfLine);
    }

    IStmt target = getDefaultTarget();
    builder.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " ").append(target == this ? "self" : target).append(";")
        .append(endOfLine);

    builder.append("}");

    return builder.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.LOOKUPSWITCH);
    up.literal("(");
    keyBox.toString(up);
    up.literal(")");
    up.newline();
    up.literal("{");
    up.newline();
    for (int i = 0; i < lookupValues.size(); i++) {
      up.literal("    ");
      up.literal(Jimple.CASE);
      up.literal(" ");
      up.constant(lookupValues.get(i));
      up.literal(": ");
      up.literal(Jimple.GOTO);
      up.literal(" ");
      targetBoxes.get(i).toString(up);
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

  public int getLookupValue(int index) {
    return lookupValues.get(index).value;
  }

  public List<IntConstant> getLookupValues() {
    return Collections.unmodifiableList(lookupValues);
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseLookupSwitchStmt(this);
  }

  @Override
  public boolean equivTo(Object o) {
    if (!(o instanceof JLookupSwitchStmt)) {
      return false;
    }

    JLookupSwitchStmt lsw = (JLookupSwitchStmt) o;
    if (lookupValues.size() != lsw.getLookupValues().size()) {
      return false;
    }
    Iterator<IntConstant> lvIterator = lookupValues.iterator();
    for (IntConstant lvOther : lsw.getLookupValues()) {
      if (!lvOther.equivTo(lvIterator.next())) {
        return false;
      }
    }

    return super.equivTo((AbstractSwitchStmt) o);
  }

  @Override
  public int equivHashCode() {
    int res = 7;
    int prime = 31;

    for (IntConstant lv : lookupValues) {
      res = res * prime + lv.equivHashCode();
    }

    return res + prime * super.equivHashCode();
  }

  @Override
  public boolean equivTo(Object o, Comparator<Object> comparator) {
    return comparator.compare(this, o) == 0;
  }
}
