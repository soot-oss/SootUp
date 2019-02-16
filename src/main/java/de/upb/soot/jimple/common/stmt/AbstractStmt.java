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

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.expr.AbstractInvokeExpr;
import de.upb.soot.jimple.common.ref.JFieldRef;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractStmt implements IStmt {
  /**
   * 
   */
  private static final long serialVersionUID = 8029583017798662173L;
  private Position position;

  /** Returns a deep clone of this object. */
  @Override
  public abstract AbstractStmt clone();

  /**
   * Returns a list of Boxes containing Values used in this Unit. The list of boxes is dynamically updated as the structure
   * changes. Note that they are returned in usual evaluation order. (this is important for aggregation)
   */
  @Override
  public List<ValueBox> getUseBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Values defined in this Unit. The list of boxes is dynamically updated as the
   * structure changes.
   */
  @Override
  public List<ValueBox> getDefBoxes() {
    return Collections.emptyList();
  }

  /**
   * Returns a list of Boxes containing Units defined in this Unit; typically branch targets. The list of boxes is
   * dynamically updated as the structure changes.
   */
  @Override
  public List<IStmtBox> getStmtBoxes() {
    return Collections.emptyList();
  }

  /** List of UnitBoxes pointing to this Unit. */
  List<IStmtBox> boxesPointingToThis = null;

  /** Returns a list of Boxes pointing to this Unit. */
  @Override
  public List<IStmtBox> getBoxesPointingToThis() {
    if (boxesPointingToThis == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(boxesPointingToThis);
  }

  @Override
  public void addBoxPointingToThis(IStmtBox b) {
    if (boxesPointingToThis == null) {
      boxesPointingToThis = new ArrayList<>();
    }
    boxesPointingToThis.add(b);
  }

  @Override
  public void removeBoxPointingToThis(IStmtBox b) {
    if (boxesPointingToThis != null) {
      boxesPointingToThis.remove(b);
    }
  }

  @Override
  public void clearStmtBoxes() {
    for (IStmtBox ub : getStmtBoxes()) {
      ub.setStmt(null);
    }
  }

  /** Returns a list of ValueBoxes, either used or defined in this Unit. */
  @Override
  public List<ValueBox> getUseAndDefBoxes() {
    List<ValueBox> useBoxes = getUseBoxes();
    List<ValueBox> defBoxes = getDefBoxes();
    if (useBoxes.isEmpty()) {
      return defBoxes;
    } else {
      if (defBoxes.isEmpty()) {
        return useBoxes;
      } else {
        List<ValueBox> valueBoxes = new ArrayList<>();
        valueBoxes.addAll(defBoxes);
        valueBoxes.addAll(useBoxes);
        return valueBoxes;
      }
    }
  }

  /** Used to implement the Switchable construct. */
  @Override
  public void accept(IVisitor sw) {
  }

  @Override
  public void redirectJumpsToThisTo(IStmt newLocation) {
    List<IStmtBox> boxesPointing = getBoxesPointingToThis();

    // important to have a static copy
    List<IStmtBox> boxesCopy = new ArrayList<>(boxesPointing);

    for (IStmtBox box : boxesCopy) {
      if (box.getStmt() != this) {
        throw new RuntimeException("Something weird's happening");
      }

      if (box.isBranchTarget()) {
        box.setStmt(newLocation);
      }
    }

  }

  @Override
  public boolean containsInvokeExpr() {
    return false;
  }

  @Override
  public AbstractInvokeExpr getInvokeExpr() {
    throw new RuntimeException("getInvokeExpr() called with no invokeExpr present!");
  }

  @Override
  public ValueBox getInvokeExprBox() {
    throw new RuntimeException("getInvokeExprBox() called with no invokeExpr present!");
  }

  @Override
  public boolean containsArrayRef() {
    return false;
  }

  @Override
  public JArrayRef getArrayRef() {
    throw new RuntimeException("getArrayRef() called with no ArrayRef present!");
  }

  @Override
  public ValueBox getArrayRefBox() {
    throw new RuntimeException("getArrayRefBox() called with no ArrayRef present!");
  }

  @Override
  public boolean containsFieldRef() {
    return false;
  }

  @Override
  public JFieldRef getFieldRef() {
    throw new RuntimeException("getFieldRef() called with no JFieldRef present!");
  }

  @Override
  public ValueBox getFieldRefBox() {
    throw new RuntimeException("getFieldRefBox() called with no JFieldRef present!");
  }

  @Override
  public void setPosition(Position position) {
    this.position = position;
  }

  public Position getPosition() {
    return position;
  }

  @Override
  public boolean equivTo(Object o, Comparator<Object> comparator) {
    return comparator.compare(this, o) == 0;
  }

}
