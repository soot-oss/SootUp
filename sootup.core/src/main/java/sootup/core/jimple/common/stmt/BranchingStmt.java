package sootup.core.jimple.common.stmt;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import javax.annotation.Nonnull;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.model.Body;

/**
 * Interface for Stmts at which the execution does not necessarily continue with the following Stmt
 * in the List
 *
 * <p>mandatory: branches() needs to be true!
 */
public abstract class BranchingStmt extends Stmt {
  public BranchingStmt(@Nonnull StmtPositionInfo positionInfo) {
    super(positionInfo);
  }

  public abstract List<Stmt> getTargetStmts(Body body);

  @Override
  public final boolean branches() {
    return true;
  }
}
