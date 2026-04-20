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

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.types.Type;

public abstract class AbstractDefinitionStmt extends AbstractStmt {

  AbstractDefinitionStmt(@NonNull StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  @NonNull
  public abstract LValue getLeftOp();

  @NonNull
  public abstract Value getRightOp();

  @NonNull
  public Type getType() {
    return getLeftOp().getType();
  }

  @Override
  @NonNull
  public Optional<LValue> getDef() {
    return Optional.of(getLeftOp());
  }

  @Override
  public final void collectUses(List<Value> collector) {
    Value rightOp = getRightOp();
    getLeftOp().collectUses(collector);
    collector.add(rightOp);
    rightOp.collectUses(collector);
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @NonNull
  public abstract FallsThroughStmt withNewDef(@NonNull Local newLocal);
}
