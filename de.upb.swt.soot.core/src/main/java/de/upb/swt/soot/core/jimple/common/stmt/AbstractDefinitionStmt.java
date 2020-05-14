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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class AbstractDefinitionStmt extends Stmt {

  @Nonnull private Value leftOp;
  @Nonnull private final Value rightOp;

  AbstractDefinitionStmt(
      @Nonnull Value leftOp, @Nonnull Value rightOp, @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.leftOp = leftOp;
    this.rightOp = rightOp;
  }

  public final Value getLeftOp() {
    return leftOp;
  }

  public Value getRightOp() {
    return rightOp;
  }

  @Override
  @Nonnull
  public List<Value> getDefs() {
    return Collections.singletonList(leftOp);
  }

  @Override
  @Nonnull
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

  @Deprecated
  private void setLeftOp(@Nonnull Value value) {
    leftOp = value;
  }

  // TODO: [ms] remove $Accessor (i.e. replace dependent logic!)
  @Deprecated
  public static class $Accessor {
    // This class deliberately starts with a $-sign to discourage usage
    // of this Soot implementation detail.

    /** Violates immutability. Only use this for legacy code. */
    @Deprecated
    public static void setLeftOp(@Nonnull AbstractDefinitionStmt box, @Nonnull Value value) {
      box.setLeftOp(value);
    }

    private $Accessor() {}
  }
}
