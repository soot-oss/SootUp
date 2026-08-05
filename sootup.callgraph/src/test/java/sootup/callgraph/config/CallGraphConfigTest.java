package sootup.callgraph.config;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * End-to-end test of the {@link CallGraphConfig#builder()} staged builder chain's in-module family
 * stages ({@code .cha()}/{@code .rta()}). {@code sootup.spark}/{@code sootup.qilin} cover the
 * {@code .into(...)} stages for their own families, since this module can't depend on them.
 */
public class CallGraphConfigTest {

  private final JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  private JavaView createView(String classPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath, SourceType.Application));
    return new JavaView(inputLocations);
  }

  @Test
  public void testChaStageBuildsCallGraph() {
    JavaView view = createView("src/test/resources/callgraph/ConcreteCall/binary");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "cvc.Class", "main", "void", Collections.singletonList("java.lang.String[]"));
    ClassType targetType = identifierFactory.getClassType("cvc.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(targetType, "target", "void", Collections.emptyList());

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(Collections.singletonList(mainMethodSignature))
            .cha()
            .build()
            .computeCallGraph();

    assertTrue(cg.containsMethod(mainMethodSignature));
    assertTrue(cg.containsMethod(targetMethod));
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(targetMethod));
  }

  @Test
  public void testRtaStageBuildsCallGraph() {
    JavaView view = createView("src/test/resources/callgraph/ConcreteCall/binary");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "cvc.Class", "main", "void", Collections.singletonList("java.lang.String[]"));
    ClassType targetType = identifierFactory.getClassType("cvc.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(targetType, "target", "void", Collections.emptyList());

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .entryPoints(Collections.singletonList(mainMethodSignature))
            .rta()
            .preInstantiatedClasses(new HashSet<>())
            .build()
            .computeCallGraph();

    assertTrue(cg.containsMethod(mainMethodSignature));
    assertTrue(cg.containsMethod(targetMethod));
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(targetMethod));
  }
}
