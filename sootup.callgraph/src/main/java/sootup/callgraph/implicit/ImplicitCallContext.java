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

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.MethodLookup;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * What an {@link ImplicitCallModel} needs to know about the analysis that uses it: how methods are
 * looked up, and which methods the analysis follows into. A call graph algorithm passes the same
 * lookup and the same notion of analyzed methods that it uses itself, so that a rule fires exactly
 * where the analysis would not see the implicit call otherwise.
 */
public interface ImplicitCallContext {

  /** The method lookup of the analysis; its view is {@link #view()}. */
  @NonNull MethodLookup methodLookup();

  /**
   * Whether the analysis follows calls into the given method, i.e. it will see the calls in its
   * body. A call graph algorithm answers this for concrete methods that its {@link
   * sootup.callgraph.scope.CallResolver} does not exclude.
   */
  boolean isAnalyzed(@NonNull SootMethod method);

  /**
   * The methods that the analysis dispatches the call at {@code stmt} in {@code caller} to, as far
   * as it knows them, e.g. those of the receiver classes it considers instantiated; empty if the
   * analysis does not say. An analysis that finds more targets later asks the model again.
   */
  @NonNull
  default Optional<Set<MethodSignature>> callTargets(
      @NonNull MethodSignature caller, @NonNull InvokableStmt stmt) {
    return Optional.empty();
  }

  /** The view of the analysis. */
  @NonNull
  default View view() {
    return methodLookup().getView();
  }

  /**
   * Returns a context with the given lookup and the given test for analyzed methods.
   *
   * @param methodLookup the method lookup of the analysis
   * @param isAnalyzed whether the analysis follows calls into a method
   */
  @NonNull
  static ImplicitCallContext of(
      @NonNull MethodLookup methodLookup, @NonNull Predicate<SootMethod> isAnalyzed) {
    return of(methodLookup, isAnalyzed, (caller, stmt) -> Optional.empty());
  }

  /**
   * Returns a context with the given lookup, the given test for analyzed methods, and the given
   * targets of calls.
   *
   * @param methodLookup the method lookup of the analysis
   * @param isAnalyzed whether the analysis follows calls into a method
   * @param callTargets the methods that the analysis dispatches a call in a method to, see {@link
   *     #callTargets}
   */
  @NonNull
  static ImplicitCallContext of(
      @NonNull MethodLookup methodLookup,
      @NonNull Predicate<SootMethod> isAnalyzed,
      @NonNull BiFunction<MethodSignature, InvokableStmt, Optional<Set<MethodSignature>>>
          callTargets) {
    return new ImplicitCallContext() {
      @Override
      @NonNull
      public Optional<Set<MethodSignature>> callTargets(
          @NonNull MethodSignature caller, @NonNull InvokableStmt stmt) {
        return callTargets.apply(caller, stmt);
      }

      @Override
      @NonNull
      public MethodLookup methodLookup() {
        return methodLookup;
      }

      @Override
      public boolean isAnalyzed(@NonNull SootMethod method) {
        return isAnalyzed.test(method);
      }
    };
  }
}
