package sootup.java.bytecode.frontend;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/** InvokeDynamics and the Operand stack.. */
public class SameVarNamesInDifferentScopesTest {
  final String directory = "src/test/resources/bugfixes/";

  @Test
  public void test() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("SameVarNamesInDifferentScopes"))
            .get()
            .getMethod("foo", Collections.emptyList())
            .get();

    ControlFlowGraph<?> controlFlowGraph = method.getBody().getControlFlowGraph();
    Assertions.assertTrue(
        controlFlowGraph.getNodes().stream()
            .anyMatch(stmt -> stmt.toString().equals("candidate_1 = \"banana\"")));
    Assertions.assertTrue(
        controlFlowGraph.getNodes().stream()
            .anyMatch(stmt -> stmt.toString().equals("candidate = 42")));
  }

  @Test
  public void testThreeSameNamesProduceSequentialSuffixes() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("ThreeCandidatesGenerated"))
            .get()
            .getMethod("foo", Collections.emptyList())
            .get();

    Set<String> localNames =
        method.getBody().getLocals().stream().map(l -> l.getName()).collect(Collectors.toSet());

    Assertions.assertTrue(localNames.contains("candidate"), "expected local 'candidate'");
    Assertions.assertTrue(localNames.contains("candidate_1"), "expected local 'candidate_1'");
    Assertions.assertTrue(localNames.contains("candidate_2"), "expected local 'candidate_2'");
    Assertions.assertFalse(
        localNames.contains("candidate_1_2"),
        "must NOT produce 'candidate_1_2' from the suffix loop bug");
  }
}
