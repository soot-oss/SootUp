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
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Solver {

  private View view;
  private CallGraph callGraph;
  private PAG pag;
  private List<MethodSignature> entryPoints;
  private SparkOptions sparkOptions;

  @Builder
  public Solver(View view, List<MethodSignature> entryPoints, SparkOptions sparkOptions) {
    this.view = view;
    this.entryPoints = entryPoints;
    this.sparkOptions = sparkOptions != null ? sparkOptions : SparkOptions.defaultOptions();
    // TODO: Build OTF CG
    this.callGraph = new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);
    this.pag = new PAG(this.sparkOptions);
  }

  public void solve() {
    callGraph
        .getMethodSignatures()
        .forEach(
            methodSignature -> view.getMethod(methodSignature).ifPresent(this::buildMethodPAG));
  }

  private void buildMethodPAG(SootMethod method) {
    MethodPAGStmtVisitor stmtVisitor =
        MethodPAGStmtVisitor.builder()
            .methodSignature(method.getSignature())
            .PAG(pag)
            .callGraph(callGraph)
            .view(view)
            .nodeFactory(new NodeFactory(sparkOptions))
            .build();
    method.getBody().getStmts().forEach(stmt -> stmt.accept(stmtVisitor));
  }
}
