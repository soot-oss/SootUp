package de.upb.swt.soot.core.jimple.basic;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

/** Partial implementation of trap (exception catcher), used within Body classes. */
public class AbstractTrap implements Trap {
  /** The exception being caught. */
  private final transient ClassType exception;

  /** The first stmt being trapped. */
  private final Stmt beginStmt;

  /** The stmt just before the last stmt being trapped. */
  private final Stmt endStmt;

  /** The stmt to which execution flows after the caught exception is triggered. */
  private final Stmt handlerStmt;

  /** The list of stmts referred to in this Trap (begin, end and handler). */
  private final List<Stmt> stmts;

  /** Creates an AbstractTrap with the given exception, handler, begin and end stmts. */
  AbstractTrap(
      @Nonnull ClassType exception,
      @Nonnull Stmt beginStmt,
      @Nonnull Stmt endStmt,
      @Nonnull Stmt handlerStmt) {
    this.exception = exception;
    this.beginStmt = beginStmt;
    this.endStmt = endStmt;
    this.handlerStmt = handlerStmt;
    this.stmts = Collections.unmodifiableList(Arrays.asList(beginStmt, endStmt, handlerStmt));
  }

  @Override
  public Stmt getBeginStmt() {
    return beginStmt;
  }

  @Override
  public Stmt getEndStmt() {
    return endStmt;
  }

  @Override
  public Stmt getHandlerStmt() {
    return handlerStmt;
  }

  @Override
  public List<Stmt> getStmts() {
    return stmts;
  }

  @Override
  public ClassType getExceptionType() {
    return exception;
  }
}
