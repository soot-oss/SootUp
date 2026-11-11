package sootup.callgraph.scope;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Markus Schmidt
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

import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.stmt.InvokableStmt;

/**
 * The calls of a method that a {@link CallResolver} lets the call graph algorithm resolve: {@link
 * #all()}, {@link #none()} or those of the statements accepted by a predicate ({@link #of}). Only
 * {@link #none()} lets the algorithm skip the method without loading its body.
 */
public final class CallFilter {

  private static final CallFilter ALL = new CallFilter(statement -> true);
  private static final CallFilter NONE = new CallFilter(statement -> false);

  @NonNull private final Predicate<InvokableStmt> resolvesStatement;

  private CallFilter(@NonNull Predicate<InvokableStmt> resolvesStatement) {
    this.resolvesStatement = resolvesStatement;
  }

  /** Resolves the calls of every statement. */
  @NonNull
  public static CallFilter all() {
    return ALL;
  }

  /** Resolves no call; the body of the method is not loaded. */
  @NonNull
  public static CallFilter none() {
    return NONE;
  }

  /**
   * Resolves the calls of the statements accepted by {@code resolvesStatement}, which is asked once
   * per invokable statement of the loaded body.
   */
  @NonNull
  public static CallFilter of(@NonNull Predicate<InvokableStmt> resolvesStatement) {
    return new CallFilter(resolvesStatement);
  }

  /** Whether no call of the method is resolved, so that its body need not be loaded. */
  public boolean resolvesNone() {
    return this == NONE;
  }

  /** Whether the calls of {@code statement} are resolved. */
  public boolean resolves(@NonNull InvokableStmt statement) {
    return resolvesStatement.test(statement);
  }
}
