package sootup.apk.frontend.entrypoint;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.util.LinkedHashSet;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.CallGraph;
import sootup.callgraph.InstantiateClassValueVisitor;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Collects the fully qualified names of every class {@code new}'d anywhere in a set of already-
 * reachable methods — the evidence steps 3/8 use to tell a genuinely-wired-up listener/callback
 * apart from one that merely happens to be linked into the APK's dex without ever being used (see
 * {@code AndroidCallbackEntryPointCreator}/{@code AndroidAsyncEntryPointCreator} and {@code
 * ANDROID_CALL_GRAPH_PLAN.md} for why "was this type ever instantiated in reachable code" is a
 * sound, not just heuristic, precondition for a listener/task ever actually firing).
 *
 * <p>Self-contained rather than reusing {@code RapidTypeAnalysisAlgorithm}'s own (larger, but
 * {@code protected}) instantiated-types tracking: this only needs the same one pattern-match {@code
 * sootup.callgraph} already exposes publicly for exactly this purpose ({@link
 * InstantiateClassValueVisitor}, used internally there for {@code <clinit>} detection), applied to
 * an already-built {@link CallGraph} rather than woven into a fresh graph-construction pass.
 */
public final class InstantiatedTypeCollector {

  private InstantiatedTypeCollector() {}

  /**
   * @param reachable a call graph whose {@link CallGraph#getMethodSignatures()} defines "already
   *     reachable" for this scan — every {@code new X(...)} in any of those methods' bodies counts
   *     as evidence {@code X} could be live.
   */
  @NonNull
  public static Set<String> collectInstantiatedClassNames(
      @NonNull View view, @NonNull CallGraph reachable) {
    Set<String> instantiated = new LinkedHashSet<>();
    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();

    for (MethodSignature methodSignature : reachable.getMethodSignatures()) {
      view.getMethod(methodSignature)
          .filter(SootMethod::hasBody)
          .ifPresent(
              method -> {
                for (Stmt stmt : method.getBody().getStmts()) {
                  if (!(stmt instanceof JAssignStmt)) {
                    continue;
                  }
                  Value rightOp = ((JAssignStmt) stmt).getRightOp();
                  instantiateVisitor.init();
                  rightOp.accept(instantiateVisitor);
                  ClassType instantiatedType = instantiateVisitor.getResult();
                  if (instantiatedType != null) {
                    instantiated.add(instantiatedType.getFullyQualifiedName());
                  }
                }
              });
    }

    return instantiated;
  }
}
