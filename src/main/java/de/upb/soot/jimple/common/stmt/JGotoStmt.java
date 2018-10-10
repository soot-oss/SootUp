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

import de.upb.soot.StmtPrinter;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.visitor.IStmtVisitor;
import de.upb.soot.jimple.visitor.IVisitor;

import java.util.Collections;
import java.util.List;

public class JGotoStmt extends AbstractStmt {
  final StmtBox targetBox;
  final List<StmtBox> targetBoxes;

  public JGotoStmt(IStmt target) {
    this(Jimple.getInstance().newStmtBox(target));
  }

  public JGotoStmt(StmtBox box) {
    targetBox = box;
    targetBoxes = Collections.singletonList(box);
  }

  @Override
  public JGotoStmt clone() {
    return new JGotoStmt(getTarget());
  }

  @Override
  public String toString() {
    IStmt t = getTarget();
    String target = "(branch)";
    if (!t.branches()) {
      target = t.toString();
    }
    return Jimple.GOTO + " [?= " + target + "]";
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.GOTO);
    up.literal(" ");
    targetBox.toString(up);
  }

  public IStmt getTarget() {
    return targetBox.getStmt();
  }

  public void setTarget(IStmt target) {
    targetBox.setStmt(target);
  }

  public StmtBox getTargetBox() {
    return targetBox;
  }

  @Override
  public List<StmtBox> getUnitBoxes() {
    return targetBoxes;
  }

  @Override
  public void accept(IVisitor sw) {
    ((IStmtVisitor) sw).caseGotoStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return false;
  }

  @Override
  public boolean branches() {
    return true;
  }

}
