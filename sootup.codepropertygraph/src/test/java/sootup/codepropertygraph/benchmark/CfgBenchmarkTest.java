package sootup.codepropertygraph.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.cfg.CfgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CfgBenchmarkTest extends BenchmarkTestSuiteBase {
  private final ClassType IfElseStatement = getClassType("IfElseStatement");
  private final ClassType TryCatchFinally = getClassType("TryCatchFinally");
  private final ClassType SwitchCaseStatement = getClassType("SwitchCaseStatement");
  private final ClassType WhileLoop = getClassType("WhileLoop");

  private CfgCreator cfgCreator;

  @BeforeEach
  public void setUp() {
    cfgCreator = new CfgCreator();
  }

  @Test
  public void testCfgForIfStatement() {
    runTest(
        "ifStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfStatement());
  }

  @Test
  public void testCfgForIfElseStatement() {
    runTest(
        "ifElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseStatement());
  }

  @Test
  public void testCfgForIfElseIfStatement() {
    runTest(
        "ifElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseIfStatement());
  }

  @Test
  public void testCfgForIfElseCascadingStatement() {
    runTest(
        "ifElseCascadingStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingStatement());
  }

  @Test
  public void testCfgForIfElseCascadingElseIfStatement() {
    runTest(
        "ifElseCascadingElseIfStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfStatement());
  }

  @Test
  public void testCfgForIfElseCascadingElseIfInElseStatement() {
    runTest(
        "ifElseCascadingElseIfInElseStatement",
        IfElseStatement,
        "int",
        Collections.singletonList("int"),
        getExpectedDotGraphForIfElseCascadingElseIfInElseStatement());
  }

  @Test
  public void testCfgForTryCatch() {
    runTest(
        "tryCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatch());
  }

  @Test
  public void testCfgForTryCatchNested() {
    runTest(
        "tryCatchNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNested());
  }

  @Test
  public void testCfgForTryCatchFinallyNested() {
    runTest(
        "tryCatchFinallyNested",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNested());
  }

  @Test
  public void testCfgForTryCatchFinallyNestedInFinally() {
    runTest(
        "tryCatchFinallyNestedInFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInFinally());
  }

  @Test
  public void testCfgForTryCatchFinallyCombined() {
    runTest(
        "tryCatchFinallyCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyCombined());
  }

  @Test
  public void testCfgForTryCatchFinallyNestedInCatch() {
    runTest(
        "tryCatchFinallyNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinallyNestedInCatch());
  }

  @Test
  public void testCfgForTryCatchFinally() {
    runTest(
        "tryCatchFinally",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchFinally());
  }

  @Test
  public void testCfgForTryCatchNestedInCatch() {
    runTest(
        "tryCatchNestedInCatch",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchNestedInCatch());
  }

  @Test
  public void testCfgForTryCatchCombined() {
    runTest(
        "tryCatchCombined",
        TryCatchFinally,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForTryCatchCombined());
  }

  @Test
  public void testCfgForSwitchCaseGroupedTargetsDefault() {
    runTest(
        "switchCaseGroupedTargetsDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargetsDefault());
  }

  @Test
  public void testCfgForSwitchWithSwitch() {
    runTest(
        "switchWithSwitch",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchWithSwitch());
  }

  @Test
  public void testCfgForSwitchCaseStatementInt() {
    runTest(
        "switchCaseStatementInt",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementInt());
  }

  @Test
  public void testCfgForSwitchCaseGroupedTargets() {
    runTest(
        "switchCaseGroupedTargets",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseGroupedTargets());
  }

  @Test
  public void testCfgForSwitchCaseStatementEnum() {
    runTest(
        "switchCaseStatementEnum",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementEnum());
  }

  @Test
  public void testCfgForSwitchCaseStatementCaseIncludingIf() {
    runTest(
        "switchCaseStatementCaseIncludingIf",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf());
  }

  @Test
  public void testCfgForSwitchCaseWithoutDefault() {
    runTest(
        "switchCaseWithoutDefault",
        SwitchCaseStatement,
        "void",
        Collections.emptyList(),
        getExpectedDotGraphForSwitchCaseWithoutDefault());
  }

  @Test
  public void testCfgForWhileLoop() {
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
    PropertyGraph graph = cfgCreator.createGraph(method);

    String actualDotGraph = normalizeDotGraph(graph.toDotGraph());
    String expectedNormalizedDotGraph = normalizeDotGraph(expectedDotGraph);

    assertEquals(
        expectedNormalizedDotGraph, actualDotGraph, "DOT graph should match the expected output");
  }

  private String normalizeDotGraph(String dotGraph) {
    return dotGraph.replaceAll("\\s+", "").replaceAll("[\\r\\n]+", "");
  }

  private String getExpectedDotGraphForIfStatement() {
    return "digraph cfg_ifStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"4\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"5\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseStatement() {
    return "digraph cfg_ifElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"7\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"5\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"6\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseIfStatement() {
    return "digraph cfg_ifElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"10\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"10\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"8\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"9\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"3\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"7\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingStatement() {
    return "digraph cfg_ifElseCascadingStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"10\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"10\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"4\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"9\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"7\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"8\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfStatement() {
    return "digraph cfg_ifElseCascadingElseIfStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 13\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"10\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"11\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"12\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"6\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"9\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForIfElseCascadingElseIfInElseStatement() {
    return "digraph cfg_ifElseCascadingElseIfInElseStatement {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"if l1 &lt;= 123\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"if l1 &gt;= 42\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 := @parameter0: int\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 21\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 22\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = 23\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"return l2\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"this := @this: IfElseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"11\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"12\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"6\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"9\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"10\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatch() {
    return "digraph cfg_tryCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = $stack4\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"9\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNested() {
    return "digraph cfg_tryCatchNested {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l2 = $stack5\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l2 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"5\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"5\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"5\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"8\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNested() {
    return "digraph cfg_tryCatchFinallyNested {\n"
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
        + "	\"11\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"l2 = $stack12\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"l2 = $stack9\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"l3 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"29\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"30\" [label=\"virtualinvoke $stack13.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"31\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"32\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"33\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"34\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"28\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"23\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"25\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"25\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"29\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"26\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"28\" -> \"17\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"28\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"29\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"22\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"33\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"34\" -> \"27\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"30\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"31\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"32\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"33\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"24\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"34\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInFinally() {
    return "digraph cfg_tryCatchFinallyNestedInFinally {\n"
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
        + "	\"17\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"29\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"30\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"31\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"32\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"33\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"34\" [label=\"l2 = $stack13\", fillcolor=\"lightblue\"];\n"
        + "	\"35\" [label=\"l2 = $stack15\", fillcolor=\"lightblue\"];\n"
        + "	\"36\" [label=\"l2 = $stack19\", fillcolor=\"lightblue\"];\n"
        + "	\"37\" [label=\"l3 = $stack10\", fillcolor=\"lightblue\"];\n"
        + "	\"38\" [label=\"l4 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"39\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"40\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"41\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"42\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"43\" [label=\"virtualinvoke $stack12.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"44\" [label=\"virtualinvoke $stack14.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"45\" [label=\"virtualinvoke $stack16.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"46\" [label=\"virtualinvoke $stack17.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"47\" [label=\"virtualinvoke $stack18.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"48\" [label=\"virtualinvoke $stack20.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"49\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"50\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"51\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"52\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"37\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"36\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"48\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"49\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"50\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"51\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"38\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"52\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"39\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"39\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"39\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"42\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"39\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"41\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"27\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"25\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"26\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"27\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"27\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"27\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"28\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"29\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"15\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"43\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"33\" -> \"15\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"33\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"34\" -> \"29\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"35\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"35\" -> \"23\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"36\" -> \"28\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"37\" -> \"26\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"38\" -> \"30\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"34\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"40\" -> \"22\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"42\" -> \"33\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"43\" -> \"15\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"43\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"44\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"45\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"45\" -> \"25\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"46\" -> \"32\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"47\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"47\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"48\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"49\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"49\" -> \"24\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"49\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"44\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"50\" -> \"31\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"51\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"51\" -> \"17\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"52\" -> \"41\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"35\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"45\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"46\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"47\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyCombined() {
    return "digraph cfg_tryCatchFinallyCombined {\n"
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
        + "	\"8\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l2 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l3 = $stack6\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"22\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"25\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"25\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"23\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"17\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"24\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"25\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"18\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"18\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinallyNestedInCatch() {
    return "digraph cfg_tryCatchFinallyNestedInCatch {\n"
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
        + "	\"11\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"l1 = \\\"1finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"l2 = $stack12\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"l3 = $stack9\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"l4 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"26\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"27\" [label=\"throw l4\", fillcolor=\"lightblue\"];\n"
        + "	\"28\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"29\" [label=\"virtualinvoke $stack11.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"30\" [label=\"virtualinvoke $stack13.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"31\" [label=\"virtualinvoke $stack14.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"32\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"33\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"34\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"28\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"23\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"25\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"17\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"25\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"29\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"26\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"28\" -> \"17\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"28\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"29\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"22\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"30\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"31\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"32\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"33\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"34\" -> \"27\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"30\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"10\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"31\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"32\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"33\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"24\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"8\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"34\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchFinally() {
    return "digraph cfg_tryCatchFinally {\n"
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
        + "	\"8\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l1 = \\\"finally\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"l2 = $stack8\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"l3 = $stack6\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"throw l3\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"virtualinvoke $stack10.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"22\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"23\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"24\" [label=\"virtualinvoke $stack7.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"25\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"16\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"22\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"22\" -> \"6\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"23\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"24\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"25\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"25\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"23\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"17\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"24\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"25\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"18\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"18\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchNestedInCatch() {
    return "digraph cfg_tryCatchNestedInCatch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack4 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack5 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack6 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"$stack7 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"$stack8 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"$stack9 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l1 = \\\"1catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l1 = \\\"1try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l1 = \\\"2catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l1 = \\\"2try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l2 = $stack7\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l3 = $stack5\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"virtualinvoke $stack4.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"virtualinvoke $stack6.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"virtualinvoke $stack8.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"21\" [label=\"virtualinvoke $stack9.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"4\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"15\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"21\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"20\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"21\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForTryCatchCombined() {
    return "digraph cfg_tryCatchCombined {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 := @caughtexception\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = &lt;java.lang.System: java.io.PrintStream out&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l1 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l1 = \\\"catch\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 = \\\"try\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = $stack4\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"this := @this: TryCatchFinally\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"virtualinvoke $stack3.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"virtualinvoke $stack5.&lt;java.io.PrintStream: void println(java.lang.String)&gt;(l1)\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"1\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"9\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"2\"[label=\"cfg_except\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargetsDefault() {
    return "digraph cfg_switchCaseGroupedTargetsDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 = 8\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"first\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"other\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = \\\"second\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"7\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"7\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"4\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"6\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchWithSwitch() {
    return "digraph cfg_switchWithSwitch {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l1 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = -1\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = 220\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"l2 = 230\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"l2 = 240\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"16\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"17\" [label=\"switch(l1) \\{     case 10:     case 20:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"18\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"19\" [label=\"switch(l1) \\{     case 20:     case 30:     case 40:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"20\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"19\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"1\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"10\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"17\" -> \"9\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"11\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"15\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"17\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"18\" -> \"8\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"12\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"13\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"14\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"19\" -> \"3\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"1\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"20\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"3\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"3\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"16\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"18\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"16\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementInt() {
    return "digraph cfg_switchCaseStatementInt {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 = 5\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"invalid\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = \\\"one\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = \\\"three\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = \\\"two\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"9\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"5\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"6\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"7\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"8\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"9\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"9\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"10\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"9\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseGroupedTargets() {
    return "digraph cfg_switchCaseGroupedTargets {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"l1 = 7\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l2 = \\\"first\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"second\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"5\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"3\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"4\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"5\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementEnum() {
    return "digraph cfg_switchCaseStatementEnum {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"$stack3 = &lt;SwitchCaseStatement$1: int[] $SwitchMap$SwitchCaseStatement$Color&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"$stack4 = virtualinvoke l1.&lt;SwitchCaseStatement$Color: int ordinal()&gt;()\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"$stack5 = $stack3[$stack4]\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l1 = &lt;SwitchCaseStatement$Color: SwitchCaseStatement$Color RED&gt;\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = \\\"\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = \\\"green\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = \\\"invalid\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = \\\"red\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"switch($stack5) \\{     case 1:     case 2:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"10\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"8\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"9\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"13\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"12\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"11\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"11\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"11\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseStatementCaseIncludingIf() {
    return "digraph cfg_switchCaseStatementCaseIncludingIf {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"if l1 != 666\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l1 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"l2 = -1\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"l2 = 1\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"l2 = 11\", fillcolor=\"lightblue\"];\n"
        + "	\"10\" [label=\"l2 = 12\", fillcolor=\"lightblue\"];\n"
        + "	\"11\" [label=\"l2 = 2\", fillcolor=\"lightblue\"];\n"
        + "	\"12\" [label=\"l2 = 3\", fillcolor=\"lightblue\"];\n"
        + "	\"13\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"14\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"15\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"10\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"11\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"12\" -> \"4\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"11\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"12\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"7\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"14\" -> \"8\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"15\" -> \"6\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"13\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"10\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"9\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"14\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"13\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForSwitchCaseWithoutDefault() {
    return "digraph cfg_switchCaseWithoutDefault {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 = 6\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l2 = \\\"one\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = \\\"three\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"l2 = \\\"two\\\"\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"8\" [label=\"switch(l1) \\{     case 1:     case 2:     case 3:     default:  \\}\", fillcolor=\"lightblue\"];\n"
        + "	\"9\" [label=\"this := @this: SwitchCaseStatement\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"7\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"7\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"8\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"7\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"6\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"4\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"5\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"6\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"8\" -> \"7\"[label=\"cfg_switch\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"9\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }

  private String getExpectedDotGraphForWhileLoop() {
    return "digraph cfg_whileLoop {\n"
        + "	rankdir=TB;\n"
        + "	node [style=filled, shape=record];\n"
        + "	edge [style=filled]\n"
        + "	\"1\" [label=\"goto\", fillcolor=\"lightblue\"];\n"
        + "	\"2\" [label=\"if l1 &lt;= l2\", fillcolor=\"lightblue\"];\n"
        + "	\"3\" [label=\"l1 = 10\", fillcolor=\"lightblue\"];\n"
        + "	\"4\" [label=\"l1 = l1 + -1\", fillcolor=\"lightblue\"];\n"
        + "	\"5\" [label=\"l2 = 0\", fillcolor=\"lightblue\"];\n"
        + "	\"6\" [label=\"return\", fillcolor=\"lightblue\"];\n"
        + "	\"7\" [label=\"this := @this: WhileLoop\", fillcolor=\"lightblue\"];\n"
        + "	\"1\" -> \"2\"[label=\"cfg_goto\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"4\"[label=\"cfg_false\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"2\" -> \"6\"[label=\"cfg_true\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"3\" -> \"5\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"4\" -> \"1\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"5\" -> \"2\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "	\"7\" -> \"3\"[label=\"cfg_next\", color=\"black\", fontcolor=\"black\"];\n"
        + "}\n";
  }
}
