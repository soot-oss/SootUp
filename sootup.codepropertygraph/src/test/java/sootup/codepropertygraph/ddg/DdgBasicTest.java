package sootup.codepropertygraph.ddg;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class DdgBasicTest extends BenchmarkTestSuiteBase {
  private DdgCreator ddgCreator;

  @BeforeEach
  public void setUp() {
    ddgCreator = new DdgCreator();
  }

  @Test
  public void testDdgForReassignment() {
    String className = "codepropertygraph.Reassignment";
    runTest(
        "calculate",
        getClassType(className),
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForCalculate());
  }

  private String getExpectedDotGraphForCalculate() {
    return "digraph ddg_calculate {\n"
        + "\trankdir=TB;\n"
        + "\tnode [style=filled, shape=record];\n"
        + "\tedge [style=filled]\n"
        + "\t\"1\" [label=\"$l1 = $l1 + $l2\", fillcolor=\"lightblue\"];\n"
        + "\t\"2\" [label=\"$l1 = $l3 + $l4\", fillcolor=\"lightblue\"];\n"
        + "\t\"3\" [label=\"$l1 = 5\", fillcolor=\"lightblue\"];\n"
        + "\t\"4\" [label=\"$l2 = $l1 - $l3\", fillcolor=\"lightblue\"];\n"
        + "\t\"5\" [label=\"$l2 = $l4 - $l1\", fillcolor=\"lightblue\"];\n"
        + "\t\"6\" [label=\"$l2 = 10\", fillcolor=\"lightblue\"];\n"
        + "\t\"7\" [label=\"$l3 = $l2 * 2\", fillcolor=\"lightblue\"];\n"
        + "\t\"8\" [label=\"$l3 = $stack5 + $l4\", fillcolor=\"lightblue\"];\n"
        + "\t\"9\" [label=\"$l3 = 15\", fillcolor=\"lightblue\"];\n"
        + "\t\"10\" [label=\"$l4 = $l1\", fillcolor=\"lightblue\"];\n"
        + "\t\"11\" [label=\"$stack5 = $l1 + $l2\", fillcolor=\"lightblue\"];\n"
        + "\t\"1\" -> \"10\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"1\" -> \"4\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"10\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"10\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"10\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"11\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"2\" -> \"11\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"2\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"3\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"4\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"5\" -> \"11\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"6\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"7\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "\t\"9\" -> \"4\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}";
  }

  private void runTest(
      String methodName,
      ClassType classType,
      String returnType,
      List<String> parameters,
      String expectedDotGraph) {
    MethodSignature methodSignature =
        getMethodSignature(classType, methodName, returnType, parameters);
    Optional<? extends SootMethod> optionalMethod = getTestResourcesMethod(methodSignature);
    assertTrue(optionalMethod.isPresent(), "Method should be present");

    SootMethod method = optionalMethod.get();
    PropertyGraph graph = ddgCreator.createGraph(method);

    String actualDotGraph = normalizeDotGraph(graph.toDotGraph());
    String expectedNormalizedDotGraph = normalizeDotGraph(expectedDotGraph);

    assertEquals(
        expectedNormalizedDotGraph, actualDotGraph, "DOT graph should match the expected output");
  }

  private String normalizeDotGraph(String dotGraph) {
    return dotGraph.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "");
  }
}
