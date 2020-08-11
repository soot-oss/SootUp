package de.upb.swt.soot.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-1999 Etienne Gagnon
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
import java.util.List;

public abstract class AbstractOpStmt extends Stmt {

  protected final ValueBox opBox;
  // new attribute: later if ValueBox is deleted, then add "final" to it.
  protected Value op;

  protected AbstractOpStmt(ValueBox opBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.opBox = opBox;
    // new attribute: later if ValueBox is deleted, then fit the constructor
    this.op = opBox.getValue();
  }

  public final Value getOp() {
    return opBox.getValue();
  }

  public final ValueBox getOpBox() {
    return opBox;
  }

  @Override
  public final List<Value> getUses() {
    List<Value> list = new ArrayList<>(op.getUses());
    list.add(op);
    return list;
  }

  protected boolean equivTo(AbstractOpStmt o) {
    return opBox.getValue().equivTo(o.getOp());
  }

  @Override
  public int equivHashCode() {
    return opBox.getValue().equivHashCode();
  }
}
