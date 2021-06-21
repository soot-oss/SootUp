package de.upb.swt.soot.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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

import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.types.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class AbstractDefinitionStmt extends Stmt {

  @Nonnull private final Value leftOp;
  @Nonnull private final Value rightOp;

  AbstractDefinitionStmt(
      @Nonnull Value leftOp, @Nonnull Value rightOp, @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.leftOp = leftOp;
    this.rightOp = rightOp;
  }

  @Nonnull
  public final Value getLeftOp() {
    return leftOp;
  }

  @Nonnull
  public Value getRightOp() {
    return rightOp;
  }

  @Nonnull
  public Type getType() {
    return getLeftOp().getType();
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
}
