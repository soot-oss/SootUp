/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Etienne Gagnon
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

import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSwitchStmt extends AbstractStmt {

  /** */
  private static final long serialVersionUID = -828246813006451813L;

  protected final IStmtBox defaultTargetBox;

  protected final ValueBox keyBox;

  protected final List<IStmtBox> stmtBoxes;

  protected final IStmtBox[] targetBoxes;

  protected AbstractSwitchStmt(
      PositionInfo positionInfo,
      ValueBox keyBox,
      IStmtBox defaultTargetBox,
      IStmtBox... targetBoxes) {
    super(positionInfo);
    this.keyBox = keyBox;
    this.defaultTargetBox = defaultTargetBox;
    this.targetBoxes = targetBoxes;

    // Build up stmtBoxes
    List<IStmtBox> list = new ArrayList<>();
    stmtBoxes = Collections.unmodifiableList(list);

    Collections.addAll(list, targetBoxes);
    list.add(defaultTargetBox);
  }

  public final IStmt getDefaultTarget() {
    return defaultTargetBox.getStmt();
  }

  public final void setDefaultTarget(IStmt defaultTarget) {
    defaultTargetBox.setStmt(defaultTarget);
  }

  public final IStmtBox getDefaultTargetBox() {
    return defaultTargetBox;
  }

  public final Value getKey() {
    return keyBox.getValue();
  }

  public final ValueBox getKeyBox() {
    return keyBox;
  }

  @Override
  public final List<ValueBox> getUseBoxes() {

    List<ValueBox> list = new ArrayList<ValueBox>(keyBox.getValue().getUseBoxes());
    list.add(keyBox);

    return list;
  }

  public final int getTargetCount() {
    return targetBoxes.length;
  }

  public final IStmt getTarget(int index) {
    return targetBoxes[index].getStmt();
  }

  public final IStmtBox getTargetBox(int index) {
    return targetBoxes[index];
  }

  public final void setTarget(int index, IStmt target) {
    targetBoxes[index].setStmt(target);
  }

  /** Returns a list targets of type Stmt. */
  public final List<IStmt> getTargets() {
    List<IStmt> targets = new ArrayList<>();

    for (IStmtBox element : targetBoxes) {
      targets.add(element.getStmt());
    }

    return targets;
  }

  /**
   * Sets the setStmt box for targetBoxes array.
   *
   * @param targets A list of type Stmt.
   */
  public final void setTargets(List<? extends IStmt> targets) {
    for (int i = 0; i < targets.size(); i++) {
      targetBoxes[i].setStmt(targets.get(i));
    }
  }

  /**
   * Sets the setStmt box for targetBoxes array.
   *
   * @param targets An array of type Stmt.
   */
  public final void setTargets(IStmt[] targets) {
    for (int i = 0; i < targets.length; i++) {
      targetBoxes[i].setStmt(targets[i]);
    }
  }

  @Override
  public final List<IStmtBox> getStmtBoxes() {
    return stmtBoxes;
  }

  @Override
  public final boolean fallsThrough() {
    return false;
  }

  @Override
  public final boolean branches() {
    return true;
  }

  @Override
  public int equivHashCode() {
    int prime = 31;
    int res =
        defaultTargetBox.getStmt().equivHashCode() + prime * keyBox.getValue().equivHashCode();

    for (IStmtBox lv : targetBoxes) {
      res = prime * res + lv.getStmt().equivHashCode();
    }

    return res;
  }
}
