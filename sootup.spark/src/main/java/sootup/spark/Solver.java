package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2026 Ondrej Lhotak, Kadiray Karakaya and others
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Solver {

  private View view;
  private CallGraph callGraph;
  private PAG pag;
  private PAGStmtVisitor stmtVisitor;
  private List<MethodSignature> entryPoints;

  @Builder
  public Solver(View view, List<MethodSignature> entryPoints) {
    this.view = view;
    this.entryPoints = entryPoints;
    // TODO: Build OTF CG
    this.callGraph = new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);
    this.pag = new PAG();
    this.stmtVisitor = PAGStmtVisitor.builder().PAG(pag).build();
  }

  public void solve() {
    MethodSignature methodSignature = callGraph.getEntryMethods().get(0);
    view.getMethod(methodSignature).ifPresent(this::buildMethodPAG);
  }

  private void buildMethodPAG(SootMethod method) {

    // first add the new
    method.getBody().getStmts().stream()
        .filter(JAssignStmt.class::isInstance)
        .map(JAssignStmt.class::cast)
        .filter(s -> s.getRightOp() instanceof JNewExpr)
        .forEach(s -> s.accept(stmtVisitor));

    method.getBody().getStmts().forEach(stmt -> stmt.accept(stmtVisitor));
  }
}
