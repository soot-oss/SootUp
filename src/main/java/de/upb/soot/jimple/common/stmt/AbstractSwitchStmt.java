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

import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractSwitchStmt extends AbstractStmt {

  protected final StmtBox defaultTargetBox;

  protected final ValueBox keyBox;

  protected final List<StmtBox> stmtBoxes;

  protected final StmtBox[] targetBoxes;

  protected AbstractSwitchStmt(ValueBox keyBox, StmtBox defaultTargetBox, StmtBox... targetBoxes) {
    this.keyBox = keyBox;
    this.defaultTargetBox = defaultTargetBox;
    this.targetBoxes = targetBoxes;

    // Build up stmtBoxes
    List<StmtBox> list = new ArrayList<StmtBox>();
    stmtBoxes = Collections.unmodifiableList(list);

    Collections.addAll(list, targetBoxes);
    list.add(defaultTargetBox);
  }

  public final Stmt getDefaultTarget() {
    return defaultTargetBox.getStmt();
  }

  public final void setDefaultTarget(Stmt defaultTarget) {
    defaultTargetBox.setStmt(defaultTarget);
  }

  public final StmtBox getDefaultTargetBox() {
    return defaultTargetBox;
  }

  public final Value getKey() {
    return keyBox.getValue();
  }

  public final void setKey(Value key) {
    keyBox.setValue(key);
  }

  public final ValueBox getKeyBox() {
    return keyBox;
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<ValueBox>();

    list.addAll(keyBox.getValue().getUseBoxes());
    list.add(keyBox);

    return list;
  }

  public final int getTargetCount() {
    return targetBoxes.length;
  }

  public final Stmt getTarget(int index) {
    return targetBoxes[index].getStmt();
  }

  public final StmtBox getTargetBox(int index) {
    return targetBoxes[index];
  }

  public final void setTarget(int index, Stmt target) {
    targetBoxes[index].setStmt(target);
  }

  public final List<Stmt> getTargets() {
    List<Stmt> targets = new ArrayList<Stmt>();

    for (StmtBox element : targetBoxes) {
      targets.add(element.getStmt());
    }

    return targets;
  }

  public final void setTargets(List<? extends Stmt> targets) {
    for (int i = 0; i < targets.size(); i++) {
      targetBoxes[i].setStmt(targets.get(i));
    }
  }

  public final void setTargets(Stmt[] targets) {
    for (int i = 0; i < targets.length; i++) {
      targetBoxes[i].setStmt(targets[i]);
    }
  }

  @Override
  public final List<StmtBox> getUnitBoxes() {
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
}
