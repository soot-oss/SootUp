package de.upb.swt.soot.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999 Patrick Lam
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractDefinitionStmt extends Stmt {

  private final ValueBox leftBox;
  private final ValueBox rightBox;

  // new attributes: later if ValueBox is deleted, then add "final" to it.
  private Value leftOp;
  private Value rightOp;

  AbstractDefinitionStmt(ValueBox leftBox, ValueBox rightBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.leftBox = leftBox;
    this.rightBox = rightBox;

    // new attribute: later if ValueBox is deleted, then fit the constructor.
    this.leftOp = leftBox.getValue();
    this.rightOp = rightBox.getValue();
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
  public final List<Value> getDefs() {
    return Collections.singletonList(leftOp);
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(leftOp.getUses());
    list.add(rightOp);
    list.addAll(rightOp.getUses());
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
