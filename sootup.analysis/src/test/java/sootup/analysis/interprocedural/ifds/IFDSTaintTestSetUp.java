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

import static org.junit.Assert.assertNotNull;

import heros.InterproceduralCFG;
import sootup.analysis.interprocedural.icfg.JimpleBasedInterproceduralCFG;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

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
        new JimpleBasedInterproceduralCFG(view, entryMethodSignature, false, false);
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
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation("src/test/resources/taint/binary"))
            .build();

    view = javaProject.createView();

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType(targetTestClassName);

    SootClass<?> sc = view.getClass(mainClassSignature).get();
    entryMethod =
        sc.getMethods().stream().filter(e -> e.getName().equals("entryPoint")).findFirst().get();

    entryMethodSignature = entryMethod.getSignature();
    assertNotNull(entryMethod);
  }
}
