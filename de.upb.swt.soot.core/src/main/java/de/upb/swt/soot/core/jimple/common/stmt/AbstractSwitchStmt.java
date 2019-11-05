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

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSwitchStmt extends AbstractStmt {

  private final StmtBox defaultTargetBox;

  private final ValueBox keyBox;

  private final List<StmtBox> stmtBoxes;

  private final StmtBox[] targetBoxes;

  protected AbstractSwitchStmt(
      StmtPositionInfo positionInfo,
      ValueBox keyBox,
      StmtBox defaultTargetBox,
      StmtBox... targetBoxes) {
    super(positionInfo);
    this.keyBox = keyBox;
    this.defaultTargetBox = defaultTargetBox;
    this.targetBoxes = targetBoxes;

    // Build up stmtBoxes
    List<StmtBox> list = new ArrayList<>();
    stmtBoxes = Collections.unmodifiableList(list);

    Collections.addAll(list, targetBoxes);
    list.add(defaultTargetBox);
  }

  public final Stmt getDefaultTarget() {
    return defaultTargetBox.getStmt();
  }

  @Deprecated
  private void setDefaultTarget(Stmt defaultTarget) {
    StmtBox.$Accessor.setStmt(defaultTargetBox, defaultTarget);
  }

  protected final StmtBox getDefaultTargetBox() {
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

    List<ValueBox> list = new ArrayList<>(keyBox.getValue().getUseBoxes());
    list.add(keyBox);

    return list;
  }

  public final int getTargetCount() {
    return targetBoxes.length;
  }

  public final Stmt getTarget(int index) {
    return targetBoxes[index].getStmt();
  }

  protected final StmtBox getTargetBox(int index) {
    return targetBoxes[index];
  }

  /** Returns a list targets of type Stmt. */
  public final List<Stmt> getTargets() {
    List<Stmt> targets = new ArrayList<>();

    for (StmtBox element : targetBoxes) {
      targets.add(element.getStmt());
    }

    return targets;
  }

  /**
   * Violates immutability. Only use in legacy code. Sets the setStmt box for targetBoxes array.
   *
   * @param targets A list of type Stmt.
   */
  @Deprecated
  private void setTargets(List<? extends Stmt> targets) {
    for (int i = 0; i < targets.size(); i++) {
      StmtBox.$Accessor.setStmt(targetBoxes[i], targets.get(i));
    }
  }

  @Override
  public final List<StmtBox> getStmtBoxes() {
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

    for (StmtBox lv : targetBoxes) {
      res = prime * res + lv.getStmt().equivHashCode();
    }

    return res;
  }

  /** This class is for internal use only. It will be removed in the future. */
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setTargets(AbstractSwitchStmt stmt, List<? extends Stmt> targets) {
      stmt.setTargets(targets);
    }

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setDefaultTarget(AbstractSwitchStmt stmt, Stmt defaultTarget) {
      stmt.setDefaultTarget(defaultTarget);
    }

    private $Accessor() {}
  }
}
