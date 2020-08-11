package de.upb.swt.soot.core.jimple.basic;

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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.Copyable;
import javax.annotation.Nonnull;

/**
 * Represents a try-catch construct.
 *
 * <p>Prefer to use the factory methods in {@link Jimple}.
 */
public final class JTrap extends AbstractTrap implements Copyable {

  public JTrap(
      @Nonnull ClassType exception,
      @Nonnull Stmt beginStmt, // inclusive
      @Nonnull Stmt endStmt, // exclusive!
      @Nonnull Stmt handlerStmt) {
    super(exception, beginStmt, endStmt, handlerStmt);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder("Trap :");
    buf.append("\nbegin  : ");
    buf.append(getBeginStmt());
    buf.append("\nend    : ");
    buf.append(getEndStmt());
    buf.append("\nhandler: ");
    buf.append(getHandlerStmt());
    return new String(buf);
  }

  @Nonnull
  public JTrap withException(@Nonnull ClassType exception) {
    return new JTrap(exception, getBeginStmt(), getEndStmt(), getHandlerStmt());
  }

  @Nonnull
  public JTrap withBeginStmt(@Nonnull Stmt beginStmt) {
    return new JTrap(getExceptionType(), beginStmt, getEndStmt(), getHandlerStmt());
  }

  @Nonnull
  public JTrap withHandlerStmt(@Nonnull Stmt handlerStmt) {
    return new JTrap(getExceptionType(), getBeginStmt(), getEndStmt(), handlerStmt);
  }

  @Nonnull
  public JTrap withEndStmt(@Nonnull Stmt endStmt) {
    return new JTrap(getExceptionType(), getBeginStmt(), endStmt, getHandlerStmt());
  }
}
