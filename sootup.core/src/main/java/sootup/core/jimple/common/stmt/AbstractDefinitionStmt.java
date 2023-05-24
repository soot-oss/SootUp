package sootup.core.jimple.common.stmt;

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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.types.Type;

public abstract class AbstractDefinitionStmt<L extends Value, R extends Value> extends Stmt {

  @Nonnull private final L leftOp;
  @Nonnull private final R rightOp;

  AbstractDefinitionStmt(
      @Nonnull L leftOp, @Nonnull R rightOp, @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.leftOp = leftOp;
    this.rightOp = rightOp;
  }

  @Nonnull
  public final L getLeftOp() {
    return leftOp;
  }

  @Nonnull
  public R getRightOp() {
    return rightOp;
  }

  @Nonnull
  public Type getType() {
    return getLeftOp().getType();
  }

  @Override
  @Nonnull
  public List<Value> getDefs() {
    final List<Value> defs = new ArrayList<>();
    defs.add(leftOp);
    return defs;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    final List<Value> defsuses = leftOp.getUses();
    final List<Value> uses = rightOp.getUses();
    List<Value> list = new ArrayList<>(defsuses.size() + uses.size() + 1);
    list.addAll(defsuses);
    list.add(rightOp);
    list.addAll(uses);
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

  public abstract Stmt withNewDef(@Nonnull Local newLocal);
}
