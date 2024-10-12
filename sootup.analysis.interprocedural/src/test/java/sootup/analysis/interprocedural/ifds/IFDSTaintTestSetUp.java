package sootup.analysis.interprocedural.ifds;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya and others
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import heros.InterproceduralCFG;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Tag;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class IFDSTaintTestSetUp {

  protected JavaView view;
  protected MethodSignature entryMethodSignature;
  protected SootMethod entryMethod;

  private static JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solved = null;

  protected JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> executeStaticAnalysis(
      String targetTestClassName) {
    setupSoot(targetTestClassName);
    runAnalysis();
    if (solved == null) {
      throw new NullPointerException("Something went wrong solving the IFDS problem!");
    }
    return solved;
  }

  private void runAnalysis() {

    JimpleBasedInterproceduralCFG icfg =
        new JimpleBasedInterproceduralCFG(
            view, Collections.singletonList(entryMethodSignature), false, false);
    IFDSTaintAnalysisProblem problem = new IFDSTaintAnalysisProblem(icfg, entryMethod);
    JimpleIFDSSolver<?, InterproceduralCFG<Stmt, SootMethod>> solver =
        new JimpleIFDSSolver(problem);
    solver.solve(entryMethod.getDeclaringClassType().getClassName());
    solved = solver;
  }

  /*
   * This method provides the options to soot to analyse the respective
   * classes.
   */
  private void setupSoot(String targetTestClassName) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/taint/binary", SourceType.Application, Collections.emptyList()));

    view = new JavaView(inputLocations);

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType(targetTestClassName);

    SootClass sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();
    assertNotNull(entryMethod);
  }
}
