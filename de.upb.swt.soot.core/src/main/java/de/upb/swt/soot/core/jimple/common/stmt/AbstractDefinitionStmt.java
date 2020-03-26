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

package de.upb.swt.soot.core.jimple.common.stmt;

import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDefinitionStmt extends AbstractStmt {

  private final ValueBox leftBox;
  private final ValueBox rightBox;

  AbstractDefinitionStmt(ValueBox leftBox, ValueBox rightBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.leftBox = leftBox;
    this.rightBox = rightBox;
  }

  public final Value getLeftOp() {
    return leftBox.getValue();
  }

  public final Value getRightOp() {
    return rightBox.getValue();
  }

  public final ValueBox getLeftOpBox() {
    return leftBox;
  }

  public final ValueBox getRightOpBox() {
    return rightBox;
  }

  @Override
  public final List<ValueBox> getDefBoxes() {
    return Collections.singletonList(leftBox);
  }

  @Override
  public final List<ValueBox> getUseBoxes() {
    List<ValueBox> list = new ArrayList<>(getLeftOp().getUseBoxes());
    list.add(rightBox);
    list.addAll(getRightOp().getUseBoxes());
    return list;
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  public ValueBox getLeftBox() {
    return leftBox;
  }

  public ValueBox getRightBox() {
    return rightBox;
  }
}
