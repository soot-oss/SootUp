package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CdgBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType IfElseStatement = getClassType("IfElseStatement");
  private final ClassType TryCatchFinally = getClassType("TryCatchFinally");
  private final ClassType SwitchCaseStatement = getClassType("SwitchCaseStatement");
  private final ClassType WhileLoop = getClassType("WhileLoop");

  private CdgCreator cdgCreator;

  @BeforeEach
  public void setUp() {
    cdgCreator = new CdgCreator();
  }

  @Test
  public void testCdgForIfStatement() {
    runTest(
        "ifStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfStatement());
  }

  @Test
  public void testCdgForIfElseStatement() {
    runTest(
        "ifElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseStatement());
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    runTest(
        "ifElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseIfStatement());
  }

  @Test
  public void testCdgForIfElseCascadingStatement() {
    runTest(
        "ifElseCascadingStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingStatement());
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    runTest(
        "ifElseCascadingElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfStatement());
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    runTest(
        "ifElseCascadingElseIfInElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfInElseStatement());
  }

  @Test
  public void testCdgForTryCatch() {
    runTest(
        "tryCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatch());
  }

  @Test
  public void testCdgForTryCatchNested() {
    runTest(
        "tryCatchNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNested());
  }

  @Test
  public void testCdgForTryCatchFinallyNested() {
    runTest(
        "tryCatchFinallyNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNested());
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInFinally() {
    runTest(
        "tryCatchFinallyNestedInFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInFinally());
  }

  @Test
  public void testCdgForTryCatchFinallyCombined() {
    runTest(
        "tryCatchFinallyCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyCombined());
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInCatch() {
    runTest(
        "tryCatchFinallyNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInCatch());
  }

  @Test
  public void testCdgForTryCatchFinally() {
    runTest(
        "tryCatchFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinally());
  }

  @Test
  public void testCdgForTryCatchNestedInCatch() {
    runTest(
        "tryCatchNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNestedInCatch());
  }

  @Test
  public void testCdgForTryCatchCombined() {
    runTest(
        "tryCatchCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchCombined());
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargetsDefault() {
    runTest(
        "switchCaseGroupedTargetsDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargetsDefault());
  }

  @Test
  public void testCdgForSwitchWithSwitch() {
    runTest(
        "switchWithSwitch",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchWithSwitch());
  }

  @Test
  public void testCdgForSwitchCaseStatementInt() {
    runTest(
        "switchCaseStatementInt",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementInt());
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargets() {
    runTest(
        "switchCaseGroupedTargets",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargets());
  }

  @Test
  public void testCdgForSwitchCaseStatementEnum() {
    runTest(
        "switchCaseStatementEnum",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementEnum());
  }

  @Test
  public void testCdgForSwitchCaseStatementCaseIncludingIf() {
    runTest(
        "switchCaseStatementCaseIncludingIf",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf());
  }

  @Test
  public void testCdgForSwitchCaseWithoutDefault() {
    runTest(
        "switchCaseWithoutDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseWithoutDefault());
  }

  @Test
  public void testCdgForWhileLoop() {
    runTest(
        "whileLoop", WhileLoop, "void", Collections.emptyList(), getExpectedDotGraphForWhileLoop());
  }

  private void runTest(
      String methodName,
      ClassType classType,
      String returnType,
      List<String> parameters,
      String expectedDotGraph) {
    MethodSignature methodSignature =
        getMethodSignature(classType, methodName, returnType, parameters);
    Optional<? extends SootMethod> optionalMethod = getMinimalTestSuiteMethod(methodSignature);
    assertTrue(optionalMethod.isPresent(), "Method should be present");

    SootMethod method = optionalMethod.get();
    PropertyGraph graph = cdgCreator.createGraph(method);

    String actualDotGraph = normalizeDotGraph(graph.toDotGraph());
    String expectedNormalizedDotGraph = normalizeDotGraph(expectedDotGraph);

    assertEquals(
        expectedNormalizedDotGraph, actualDotGraph, "DOT graph should match the expected output");
  }

  private String normalizeDotGraph(String dotGraph) {
    return dotGraph.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "");
  }

  private String getExpectedDotGraphForIfStatement() {
    return "digraph cdg_ifStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseStatement() {
    return "digraph cdg_ifElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"2\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"2\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseIfStatement() {
    return "digraph cdg_ifElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"3\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"3\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingStatement() {
    return "digraph cdg_ifElseCascadingStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"3\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfStatement() {
    return "digraph cdg_ifElseCascadingElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 13\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"8\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"9\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"10\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfInElseStatement() {
    return "digraph cdg_ifElseCascadingElseIfInElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 21\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 22\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 23\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"10\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"9\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"8\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatch() {
    return "digraph cdg_tryCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNested() {
    return "digraph cdg_tryCatchNested {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNested() {
    return "digraph cdg_tryCatchFinallyNested {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInFinally() {
    return "digraph cdg_tryCatchFinallyNestedInFinally {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyCombined() {
    return "digraph cdg_tryCatchFinallyCombined {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInCatch() {
    return "digraph cdg_tryCatchFinallyNestedInCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinally() {
    return "digraph cdg_tryCatchFinally {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNestedInCatch() {
    return "digraph cdg_tryCatchNestedInCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchCombined() {
    return "digraph cdg_tryCatchCombined {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargetsDefault() {
    return "digraph cdg_switchCaseGroupedTargetsDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = \\\"first\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"other\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"second\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchWithSwitch() {
    return "digraph cdg_switchWithSwitch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = -1\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 220\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = 230\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l2 = 240\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"switch(l1) \\{     case 10:     case 20:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"switch(l1) \\{     case 20:     case 30:     case 40:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"15\" -> \"8\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"15\" -> \"9\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"10\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"14\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"15\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"17\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"16\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"17\" -> \"11\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"17\" -> \"12\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"17\" -> \"13\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"17\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"17\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementInt() {
    return "digraph cdg_switchCaseStatementInt {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"invalid\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"one\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = \\\"three\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = \\\"two\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"8\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargets() {
    return "digraph cdg_switchCaseGroupedTargets {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l2 = \\\"first\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = \\\"second\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"4\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementEnum() {
    return "digraph cdg_switchCaseStatementEnum {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = \\\"green\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"invalid\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"red\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"switch($stack5) \\{     case 1:     case 2:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf() {
    return "digraph cdg_switchCaseStatementCaseIncludingIf {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 != 666\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = -1\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"10\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"11\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"6\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"12\" -> \"7\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"8\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"5\" -> \"9\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseWithoutDefault() {
    return "digraph cdg_switchCaseWithoutDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = \\\"one\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"three\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"two\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" -> \"1\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"3\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"4\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "	\"6\" -> \"5\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForWhileLoop() {
    return "digraph cdg_whileLoop {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &lt;= l2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"cdg_next\", color=\"dodgerblue4\", fontcolor=\"dodgerblue4\"];\n"
        + "}\n";
  }
}
