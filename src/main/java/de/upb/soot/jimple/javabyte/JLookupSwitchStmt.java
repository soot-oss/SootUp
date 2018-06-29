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
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JLookupSwitchStmt extends AbstractSwitchStmt {
  /**
   * List of lookup values from the corresponding bytecode instruction, represented as IntConstants.
   */
  List<IntConstant> lookupValues;

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
    int lookupValueCount = lookupValues.size();
    List<IntConstant> clonedLookupValues = new ArrayList<IntConstant>(lookupValueCount);

    for (int i = 0; i < lookupValueCount; i++) {
      clonedLookupValues.add(i, IntConstant.getInstance(getLookupValue(i)));
    }

    return new JLookupSwitchStmt(getKey(), clonedLookupValues, getTargets(), getDefaultTarget());
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends Stmt> targets, Stmt defaultTarget) {
    this(Jimple.getInstance().newImmediateBox(key), lookupValues, getTargetBoxesArray(targets),
        Jimple.getInstance().newStmtBox(defaultTarget));
  }

  /** Constructs a new JLookupSwitchStmt. lookupValues should be a list of IntConst s. */
  public JLookupSwitchStmt(Value key, List<IntConstant> lookupValues, List<? extends StmtBox> targets,
      StmtBox defaultTarget) {
    this(Jimple.getInstance().newImmediateBox(key), lookupValues, targets.toArray(new StmtBox[targets.size()]),
        defaultTarget);
  }

  protected JLookupSwitchStmt(ValueBox keyBox, List<IntConstant> lookupValues, StmtBox[] targetBoxes,
      StmtBox defaultTargetBox) {
    super(keyBox, defaultTargetBox, targetBoxes);
    setLookupValues(lookupValues);
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    String endOfLine = " ";

    buffer.append(Jimple.LOOKUPSWITCH + "(" + keyBox.getValue().toString() + ")" + endOfLine);

    buffer.append("{" + endOfLine);

    for (int i = 0; i < lookupValues.size(); i++) {
      Stmt target = getTarget(i);
      buffer.append("    " + Jimple.CASE + " " + lookupValues.get(i) + ": " + Jimple.GOTO + " "
          + (target == this ? "self" : target) + ";" + endOfLine);
    }

    Stmt target = getDefaultTarget();
    buffer.append("    " + Jimple.DEFAULT + ": " + Jimple.GOTO + " " + (target == this ? "self" : target) + ";" + endOfLine);

    buffer.append("}");

    return buffer.toString();
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
    for (int i = 0; i < lookupValues.size(); i++) {
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
    this.lookupValues = new ArrayList<IntConstant>(lookupValues);
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

}
