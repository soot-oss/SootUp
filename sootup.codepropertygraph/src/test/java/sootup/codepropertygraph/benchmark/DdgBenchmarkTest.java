package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class DdgBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType IfElseStatement = getClassType("IfElseStatement");
  private final ClassType TryCatchFinally = getClassType("TryCatchFinally");
  private final ClassType SwitchCaseStatement = getClassType("SwitchCaseStatement");
  private final ClassType WhileLoop = getClassType("WhileLoop");

  private DdgCreator ddgCreator;

  @BeforeEach
  public void setUp() {
    ddgCreator = new DdgCreator();
  }

  @Test
  public void testDdgForIfStatement() {
    runTest(
        "ifStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfStatement());
  }

  @Test
  public void testDdgForIfElseStatement() {
    runTest(
        "ifElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseStatement());
  }

  @Test
  public void testDdgForIfElseIfStatement() {
    runTest(
        "ifElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseIfStatement());
  }

  @Test
  public void testDdgForIfElseCascadingStatement() {
    runTest(
        "ifElseCascadingStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingStatement());
  }

  @Test
  public void testDdgForIfElseCascadingElseIfStatement() {
    runTest(
        "ifElseCascadingElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfStatement());
  }

  @Test
  public void testDdgForIfElseCascadingElseIfInElseStatement() {
    runTest(
        "ifElseCascadingElseIfInElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfInElseStatement());
  }

  @Test
  public void testDdgForTryCatch() {
    runTest(
        "tryCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatch());
  }

  @Test
  public void testDdgForTryCatchNested() {
    runTest(
        "tryCatchNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNested());
  }

  @Test
  public void testDdgForTryCatchFinallyNested() {
    runTest(
        "tryCatchFinallyNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNested());
  }

  @Test
  public void testDdgForTryCatchFinallyNestedInFinally() {
    runTest(
        "tryCatchFinallyNestedInFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInFinally());
  }

  @Test
  public void testDdgForTryCatchFinallyCombined() {
    runTest(
        "tryCatchFinallyCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyCombined());
  }

  @Test
  public void testDdgForTryCatchFinallyNestedInCatch() {
    runTest(
        "tryCatchFinallyNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInCatch());
  }

  @Test
  public void testDdgForTryCatchFinally() {
    runTest(
        "tryCatchFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinally());
  }

  @Test
  public void testDdgForTryCatchNestedInCatch() {
    runTest(
        "tryCatchNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNestedInCatch());
  }

  @Test
  public void testDdgForTryCatchCombined() {
    runTest(
        "tryCatchCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchCombined());
  }

  @Test
  public void testDdgForSwitchCaseGroupedTargetsDefault() {
    runTest(
        "switchCaseGroupedTargetsDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargetsDefault());
  }

  @Test
  public void testDdgForSwitchWithSwitch() {
    runTest(
        "switchWithSwitch",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchWithSwitch());
  }

  @Test
  public void testDdgForSwitchCaseStatementInt() {
    runTest(
        "switchCaseStatementInt",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementInt());
  }

  @Test
  public void testDdgForSwitchCaseGroupedTargets() {
    runTest(
        "switchCaseGroupedTargets",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargets());
  }

  @Test
  public void testDdgForSwitchCaseStatementEnum() {
    runTest(
        "switchCaseStatementEnum",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementEnum());
  }

  @Test
  public void testDdgForSwitchCaseStatementCaseIncludingIf() {
    runTest(
        "switchCaseStatementCaseIncludingIf",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf());
  }

  @Test
  public void testDdgForSwitchCaseWithoutDefault() {
    runTest(
        "switchCaseWithoutDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseWithoutDefault());
  }

  @Test
  public void testDdgForWhileLoop() {
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
    PropertyGraph graph = ddgCreator.createGraph(method);

    String actualDotGraph = normalizeDotGraph(graph.toDotGraph());
    String expectedNormalizedDotGraph = normalizeDotGraph(expectedDotGraph);

    assertEquals(
        expectedNormalizedDotGraph, actualDotGraph, "DOT graph should match the expected output");
  }

  private String normalizeDotGraph(String dotGraph) {
    return dotGraph.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "");
  }

  private String getExpectedDotGraphForIfStatement() {
    return "digraph ddg_ifStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseStatement() {
    return "digraph ddg_ifElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseIfStatement() {
    return "digraph ddg_ifElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingStatement() {
    return "digraph ddg_ifElseCascadingStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfStatement() {
    return "digraph ddg_ifElseCascadingElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 13\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfInElseStatement() {
    return "digraph ddg_ifElseCascadingElseIfInElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 21\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 22\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 23\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"9\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatch() {
    return "digraph ddg_tryCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = $stack4\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"6\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNested() {
    return "digraph ddg_tryCatchNested {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = $stack5\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"11\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"12\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNested() {
    return "digraph ddg_tryCatchFinallyNested {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack10 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack11 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack12 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack13 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"$stack9 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"l2 = $stack12\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"l2 = $stack9\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"l3 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"virtualinvoke $stack13.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"22\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"11\" -> \"22\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"12\" -> \"23\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"13\" -> \"27\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"14\" -> \"28\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"15\" -> \"25\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"16\" -> \"24\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"17\" -> \"26\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"23\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"20\" -> \"21\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"24\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"25\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"26\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"27\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"28\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInFinally() {
    return "digraph ddg_tryCatchFinallyNestedInFinally {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack10 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack11 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack12 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack13 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack14 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack15 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"$stack16 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"$stack17 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"$stack18 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"$stack19 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"$stack20 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"$stack7 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"$stack8 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"$stack9 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"l2 = $stack13\", fillcolor=\"lightblue\"];\n"
        + "	\"29\" [label=\"l2 = $stack15\", fillcolor=\"lightblue\"];\n"
        + "	\"30\" [label=\"l2 = $stack19\", fillcolor=\"lightblue\"];\n"
        + "	\"31\" [label=\"l3 = $stack10\", fillcolor=\"lightblue\"];\n"
        + "	\"32\" [label=\"l4 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"33\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"34\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"35\" [label=\"virtualinvoke $stack12.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"36\" [label=\"virtualinvoke $stack14.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"37\" [label=\"virtualinvoke $stack16.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"38\" [label=\"virtualinvoke $stack17.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"39\" [label=\"virtualinvoke $stack18.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"40\" [label=\"virtualinvoke $stack20.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"41\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"42\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"43\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"44\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"31\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"30\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"11\" -> \"40\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"12\" -> \"41\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"13\" -> \"42\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"14\" -> \"43\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"15\" -> \"32\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"16\" -> \"44\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"17\" -> \"37\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"18\" -> \"34\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"19\" -> \"38\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"34\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"20\" -> \"42\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"21\" -> \"41\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"22\" -> \"36\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"23\" -> \"40\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"24\" -> \"44\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"25\" -> \"35\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"26\" -> \"39\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"27\" -> \"43\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"35\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"31\" -> \"33\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"28\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"36\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"29\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"37\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"38\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"39\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyCombined() {
    return "digraph ddg_tryCatchFinallyCombined {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack10 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack6 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack7 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack8 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"$stack9 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l2 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l3 = $stack6\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"11\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"12\" -> \"17\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"14\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"17\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInCatch() {
    return "digraph ddg_tryCatchFinallyNestedInCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack10 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack11 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack12 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack13 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack14 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"$stack9 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"l2 = $stack12\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"l3 = $stack9\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"l4 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"throw l4\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"virtualinvoke $stack13.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"virtualinvoke $stack14.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"22\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"11\" -> \"24\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"12\" -> \"23\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"13\" -> \"27\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"14\" -> \"28\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"15\" -> \"26\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"16\" -> \"22\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"17\" -> \"25\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"23\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"20\" -> \"21\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"24\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"25\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"26\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"27\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"28\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinally() {
    return "digraph ddg_tryCatchFinally {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack10 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack6 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack7 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack8 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"$stack9 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l2 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l3 = $stack6\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"11\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"12\" -> \"17\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"14\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"17\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"18\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"19\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"20\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNestedInCatch() {
    return "digraph ddg_tryCatchNestedInCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack5 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack9 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l3 = $stack5\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"10\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"12\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"11\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"6\" -> \"16\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"7\" -> \"15\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"8\" -> \"13\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"9\" -> \"14\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchCombined() {
    return "digraph ddg_tryCatchCombined {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = $stack4\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"6\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"8\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"5\" -> \"7\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargetsDefault() {
    return "digraph ddg_switchCaseGroupedTargetsDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"l1 = 8\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchWithSwitch() {
    return "digraph ddg_switchWithSwitch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"l1 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"switch(l1) \\{     case 10:     case 20:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"switch(l1) \\{     case 20:     case 30:     case 40:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"1\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"1\" -> \"4\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementInt() {
    return "digraph ddg_switchCaseStatementInt {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"l1 = 5\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargets() {
    return "digraph ddg_switchCaseGroupedTargets {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"l1 = 7\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementEnum() {
    return "digraph ddg_switchCaseStatementEnum {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;SwitchCaseStatement$1: int[] $SwitchMap$SwitchCaseStatement$Color&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = virtualinvoke l1.&lt;SwitchCaseStatement$Color: int ordinal()&gt;()\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = $stack3[$stack4]\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 = &lt;SwitchCaseStatement$Color: SwitchCaseStatement$Color RED&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"switch($stack5) \\{     case 1:     case 2:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"5\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf() {
    return "digraph ddg_switchCaseStatementCaseIncludingIf {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 != 666\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseWithoutDefault() {
    return "digraph ddg_switchCaseWithoutDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"l1 = 6\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForWhileLoop() {
    return "digraph ddg_whileLoop {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &lt;= l2\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 = 10\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 = l1 + -1\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"2\" -> \"3\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"3\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "	\"4\" -> \"1\"[label=\"ddg_next\", color=\"firebrick\", fontcolor=\"firebrick\"];\n"
        + "}\n";
  }
}
