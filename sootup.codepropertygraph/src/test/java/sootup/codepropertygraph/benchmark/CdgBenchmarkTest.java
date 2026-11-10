package sootup.codepropertygraph.benchmark;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.codepropertygraph.BenchmarkTestSuiteBase;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class CdgBenchmarkTest extends BenchmarkTestSuiteBase {

  private static final String SW_1_2_3_DEF =
      "switch(l1) {     case 1:     case 2:     case 3:     default:  }";
  private static final String SW_10_20_DEF =
      "switch(l1) {     case 10:     case 20:     default:  }";
  private static final String SW_20_30_40_DEF =
      "switch(l1) {     case 20:     case 30:     case 40:     default:  }";
  private static final String SW_ENUM = "switch($stack5) {     case 1:     case 2:     default:  }";

  private final ClassType IfElseStatement = getClassType("IfElseStatement");
  private final ClassType TryCatchFinally = getClassType("TryCatchFinally");
  private final ClassType SwitchCaseStatement = getClassType("SwitchCaseStatement");
  private final ClassType WhileLoop = getClassType("WhileLoop");

  private CdgCreator cdgCreator;

  @BeforeEach
  public void setUp() {
    cdgCreator = new CdgCreator();
  }

  private PropertyGraph buildGraph(
      ClassType classType, String methodName, String returnType, String... paramTypes) {
    MethodSignature sig =
        getMethodSignature(classType, methodName, returnType, Arrays.asList(paramTypes));
    return cdgCreator.createGraph(getMinimalTestSuiteMethod(sig).orElseThrow());
  }

  /**
   * Asserts that the CDG contains exactly the given edges (order-independent, multiset semantics).
   * Each edge is expressed as "source.toString() -> dest.toString()".
   */
  private static void assertEdges(PropertyGraph graph, String... expected) {
    List<String> actual =
        graph.getEdges().stream()
            .map(e -> e.getSource() + " -> " + e.getDestination())
            .sorted()
            .collect(Collectors.toList());
    List<String> exp = Arrays.stream(expected).sorted().collect(Collectors.toList());
    assertEquals(exp, actual);
  }

  @Test
  public void testCdgForIfStatement() {
    PropertyGraph g = buildGraph(IfElseStatement, "ifStatement", "int", "int");
    assertEdges(g, "if l1 >= 42 -> l2 = 1");
  }

  @Test
  public void testCdgForIfElseStatement() {
    PropertyGraph g = buildGraph(IfElseStatement, "ifElseStatement", "int", "int");
    assertEdges(g, "if l1 >= 42 -> goto", "if l1 >= 42 -> l2 = 1", "if l1 >= 42 -> l2 = 2");
  }

  @Test
  public void testCdgForIfElseIfStatement() {
    PropertyGraph g = buildGraph(IfElseStatement, "ifElseIfStatement", "int", "int");
    assertEdges(
        g,
        "if l1 <= 123 -> goto",
        "if l1 <= 123 -> l2 = 2",
        "if l1 <= 123 -> l2 = 3",
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> if l1 <= 123",
        "if l1 >= 42 -> l2 = 1");
  }

  @Test
  public void testCdgForIfElseCascadingStatement() {
    PropertyGraph g = buildGraph(IfElseStatement, "ifElseCascadingStatement", "int", "int");
    // outer "if l1 >= 42" controls the inner "if l1 >= 42" and the else-branch (l2=3);
    // inner "if l1 >= 42" controls l2=11, l2=12, and two gotos (then/else exits)
    assertEdges(
        g,
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> if l1 >= 42",
        "if l1 >= 42 -> l2 = 11",
        "if l1 >= 42 -> l2 = 12",
        "if l1 >= 42 -> l2 = 3");
  }

  @Test
  public void testCdgForIfElseCascadingElseIfStatement() {
    PropertyGraph g = buildGraph(IfElseStatement, "ifElseCascadingElseIfStatement", "int", "int");
    // outer "if l1 >= 42" controls the middle "if l1 >= 42" and the else-branch (l2=2)
    // middle "if l1 >= 42" controls one goto, "if l1 <= 123" and l2=11
    // "if l1 <= 123" controls two gotos, l2=12, l2=13
    assertEdges(
        g,
        "if l1 <= 123 -> goto",
        "if l1 <= 123 -> goto",
        "if l1 <= 123 -> l2 = 12",
        "if l1 <= 123 -> l2 = 13",
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> if l1 <= 123",
        "if l1 >= 42 -> if l1 >= 42",
        "if l1 >= 42 -> l2 = 11",
        "if l1 >= 42 -> l2 = 2");
  }

  @Test
  public void testCdgForIfElseCascadingElseIfInElseStatement() {
    PropertyGraph g =
        buildGraph(IfElseStatement, "ifElseCascadingElseIfInElseStatement", "int", "int");
    // outermost "if l1 >= 42" controls the next "if l1 >= 42" and l2=1
    // middle "if l1 >= 42" controls one goto, "if l1 <= 123" and l2=21
    // "if l1 <= 123" controls one goto, l2=22, l2=23
    assertEdges(
        g,
        "if l1 <= 123 -> goto",
        "if l1 <= 123 -> l2 = 22",
        "if l1 <= 123 -> l2 = 23",
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> goto",
        "if l1 >= 42 -> if l1 <= 123",
        "if l1 >= 42 -> if l1 >= 42",
        "if l1 >= 42 -> l2 = 1",
        "if l1 >= 42 -> l2 = 21");
  }

  @Test
  public void testCdgForTryCatch() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatch", "void"));
  }

  @Test
  public void testCdgForTryCatchNested() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchNested", "void"));
  }

  @Test
  public void testCdgForTryCatchFinallyNested() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchFinallyNested", "void"));
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInFinally() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchFinallyNestedInFinally", "void"));
  }

  @Test
  public void testCdgForTryCatchFinallyCombined() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchFinallyCombined", "void"));
  }

  @Test
  public void testCdgForTryCatchFinallyNestedInCatch() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchFinallyNestedInCatch", "void"));
  }

  @Test
  public void testCdgForTryCatchFinally() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchFinally", "void"));
  }

  @Test
  public void testCdgForTryCatchNestedInCatch() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchNestedInCatch", "void"));
  }

  @Test
  public void testCdgForTryCatchCombined() {
    assertEdges(buildGraph(TryCatchFinally, "tryCatchCombined", "void"));
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargetsDefault() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseGroupedTargetsDefault", "void");
    assertEdges(
        g,
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> l2 = \"first\"",
        SW_1_2_3_DEF + " -> l2 = \"other\"",
        SW_1_2_3_DEF + " -> l2 = \"second\"");
  }

  @Test
  public void testCdgForSwitchWithSwitch() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchWithSwitch", "void");
    assertEdges(
        g,
        SW_10_20_DEF + " -> goto",
        SW_10_20_DEF + " -> l2 = 11",
        SW_10_20_DEF + " -> l2 = 12",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> l2 = -1",
        SW_1_2_3_DEF + " -> l2 = 2",
        SW_1_2_3_DEF + " -> l2 = 3",
        SW_1_2_3_DEF + " -> " + SW_10_20_DEF,
        SW_1_2_3_DEF + " -> " + SW_20_30_40_DEF,
        SW_20_30_40_DEF + " -> goto",
        SW_20_30_40_DEF + " -> goto",
        SW_20_30_40_DEF + " -> l2 = 220",
        SW_20_30_40_DEF + " -> l2 = 230",
        SW_20_30_40_DEF + " -> l2 = 240");
  }

  @Test
  public void testCdgForSwitchCaseStatementInt() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseStatementInt", "void");
    assertEdges(
        g,
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> l2 = \"invalid\"",
        SW_1_2_3_DEF + " -> l2 = \"one\"",
        SW_1_2_3_DEF + " -> l2 = \"three\"",
        SW_1_2_3_DEF + " -> l2 = \"two\"");
  }

  @Test
  public void testCdgForSwitchCaseGroupedTargets() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseGroupedTargets", "void");
    assertEdges(
        g,
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> l2 = \"first\"",
        SW_1_2_3_DEF + " -> l2 = \"second\"");
  }

  @Test
  public void testCdgForSwitchCaseStatementEnum() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseStatementEnum", "void");
    assertEdges(
        g,
        SW_ENUM + " -> goto",
        SW_ENUM + " -> goto",
        SW_ENUM + " -> l2 = \"green\"",
        SW_ENUM + " -> l2 = \"invalid\"",
        SW_ENUM + " -> l2 = \"red\"");
  }

  @Test
  public void testCdgForSwitchCaseStatementCaseIncludingIf() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseStatementCaseIncludingIf", "void");
    assertEdges(
        g,
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> if l1 != 666",
        SW_1_2_3_DEF + " -> l2 = -1",
        SW_1_2_3_DEF + " -> l2 = 1",
        SW_1_2_3_DEF + " -> l2 = 2",
        SW_1_2_3_DEF + " -> l2 = 3",
        "if l1 != 666 -> goto",
        "if l1 != 666 -> goto",
        "if l1 != 666 -> l2 = 11",
        "if l1 != 666 -> l2 = 12");
  }

  @Test
  public void testCdgForSwitchCaseWithoutDefault() {
    PropertyGraph g = buildGraph(SwitchCaseStatement, "switchCaseWithoutDefault", "void");
    assertEdges(
        g,
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> goto",
        SW_1_2_3_DEF + " -> l2 = \"one\"",
        SW_1_2_3_DEF + " -> l2 = \"three\"",
        SW_1_2_3_DEF + " -> l2 = \"two\"");
  }

  @Test
  public void testCdgForWhileLoop() {
    PropertyGraph g = buildGraph(WhileLoop, "whileLoop", "void");
    assertEdges(g, "if l1 <= l2 -> return");
  }
}
