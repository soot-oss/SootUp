package sootup.callgraph.implicit;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.Body;
import sootup.core.views.View;

/**
 * Models calls that a method performs implicitly, e.g. {@code Thread.start()} calling {@code run()}
 * on the thread, or {@code Executor.execute(Runnable)} calling {@code run()} on its argument. Call
 * graph algorithms resolve the returned invocations like ordinary calls and attribute the resulting
 * edges to the call statement that causes them.
 */
@FunctionalInterface
public interface ImplicitCallModel {

  /**
   * Returns the invocations that the given call statement performs implicitly. Each invocation
   * calls a method on a local of the given body; its arguments are the values that the implicitly
   * called method receives, or {@link sootup.core.jimple.common.constant.NullConstant} for
   * parameters without a value.
   *
   * @param view the view providing the classes
   * @param body the body that contains the statement
   * @param stmt the call statement
   * @return the implicit invocations, empty if there are none
   */
  @NonNull List<AbstractInstanceInvokeExpr> resolve(
      @NonNull View view, @NonNull Body body, @NonNull InvokableStmt stmt);

  /** Returns a model without implicit calls. */
  @NonNull
  static ImplicitCallModel none() {
    return (view, body, stmt) -> List.of();
  }

  /** Returns a model in which {@code Thread.start()} calls {@code run()} on the thread. */
  @NonNull
  static ImplicitCallModel threadStart() {
    return ImplicitCalls.THREAD_START;
  }

  /**
   * Returns a model with the implicit calls of the JDK: starting threads, including the {@link
   * Runnable} passed to the thread's constructor, and executing {@link Runnable}s with an {@link
   * java.util.concurrent.Executor}.
   */
  @NonNull
  static ImplicitCallModel jdk() {
    return ImplicitCalls.JDK;
  }

  /** Returns a model with the given rules. */
  @NonNull
  static ImplicitCallModel of(@NonNull Collection<ImplicitCall> rules) {
    return new RuleBasedImplicitCallModel(rules);
  }
}
