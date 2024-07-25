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

import java.util.Optional;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;

/**
 * Interface for Stmts that could invoke a different method which will be executed before the next
 * statement is executed
 */
public interface InvokableStmt extends Stmt {

  /**
   * Checks if the invokable statement can potentially invoke a static initializer To cause a static
   * initializer call, the statement contains a static method call, a new expression, or a static
   * field reference.
   *
   * @return true if the statement can cause a static initializer call, and false if not.
   */
  boolean invokesStaticInitializer();

  /**
   * Checks if the invokable statement contains a invoke expression that defines the invoke.
   *
   * @return true if the statement contains an invoke expression, false if not
   */
  boolean containsInvokeExpr();

  /**
   * Returns the possible invoke expression in the invokable statement
   *
   * @return The optional contains the invoke expression of the invokable statement or is empty if
   *     there is no invoke expression.
   */
  Optional<AbstractInvokeExpr> getInvokeExpr();
}
