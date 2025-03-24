package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Christian Brüggemann and others
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

import java.util.Objects;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ClassType;

/**
 * Represents a try-catch construct.
 *
 * <p>Prefer to use the factory methods in {@link Jimple}.
 */
public final class Trap {

  /** The exception being caught. */
  @NonNull private final ClassType exception;

  /** The first stmt being trapped. */
  @NonNull private final Stmt beginStmt;

  /** The stmt just before the last stmt being trapped. */
  @NonNull private final Stmt endStmt;

  /** The stmt to which execution flows after the caught exception is triggered. */
  @NonNull private final Stmt handlerStmt;

  /** Creates a Trap with the given exception, handler, begin and end stmts. */
  public Trap(
      @NonNull ClassType exception,
      @NonNull Stmt beginStmt, // inclusive
      @NonNull Stmt endStmt, // exclusive!
      @NonNull Stmt handlerStmt) {

    if (beginStmt == endStmt) {
      throw new IllegalArgumentException("The covered Trap range is empty. Trap is of no use.");
    }

    this.exception = exception;
    this.beginStmt = beginStmt;
    this.endStmt = endStmt;
    this.handlerStmt = handlerStmt;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(exception);
    sb.append(" from: ").append(getBeginStmt());
    sb.append(" to: ").append(getEndStmt());
    sb.append(" handler: ").append(getHandlerStmt());
    return new String(sb);
  }

  @NonNull
  public Trap withException(@NonNull ClassType exception) {
    return new Trap(exception, getBeginStmt(), getEndStmt(), getHandlerStmt());
  }

  @NonNull
  public Trap withBeginStmt(@NonNull Stmt beginStmt) {
    return new Trap(getExceptionType(), beginStmt, getEndStmt(), getHandlerStmt());
  }

  @NonNull
  public Trap withHandlerStmt(@NonNull Stmt handlerStmt) {
    return new Trap(getExceptionType(), getBeginStmt(), getEndStmt(), handlerStmt);
  }

  @NonNull
  public Trap withEndStmt(@NonNull Stmt endStmt) {
    return new Trap(getExceptionType(), getBeginStmt(), endStmt, getHandlerStmt());
  }

  @NonNull
  public Stmt getBeginStmt() {
    return beginStmt;
  }

  @NonNull
  public Stmt getEndStmt() {
    return endStmt;
  }

  @NonNull
  public Stmt getHandlerStmt() {
    return handlerStmt;
  }

  @NonNull
  public ClassType getExceptionType() {
    return exception;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getExceptionType(), getBeginStmt(), getEndStmt(), getHandlerStmt());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Trap trap = (Trap) o;
    return exception.equals(trap.exception)
        && beginStmt.equals(trap.beginStmt)
        && Objects.equals(endStmt, trap.endStmt)
        && handlerStmt.equals(trap.handlerStmt);
  }
}
