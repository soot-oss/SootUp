package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Zun Wang and others
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
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Value;

public abstract class AbstractUnopExpr implements Expr {

  @NonNull private final Immediate op;

  AbstractUnopExpr(@NonNull Immediate op) {
    this.op = op;
  }

  @NonNull
  public Immediate getOp() {
    return op;
  }

  @Override
  public final void collectUses(List<Value> collector) {
    op.collectUses(collector);
    collector.add(op);
  }
}
