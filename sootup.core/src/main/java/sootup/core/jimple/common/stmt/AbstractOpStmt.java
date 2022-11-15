package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Markus Schmidt and others
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
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;

public abstract class AbstractOpStmt extends Stmt {

  protected final Immediate op;

  protected AbstractOpStmt(@Nonnull Immediate op, @Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.op = op;
  }

  @Nonnull
  public Immediate getOp() {
    return op;
  }

  @Override
  @Nonnull
  public final List<Value> getUses() {
    final List<Value> uses = op.getUses();
    List<Value> list = new ArrayList<>(uses.size() + 1);
    list.add(op);
    return list;
  }

  @Override
  public int equivHashCode() {
    return op.equivHashCode();
  }
}
