package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.interceptors.DeadAssignmentEliminator;
import sootup.interceptors.LocalPacker;
import sootup.java.bytecode.frontend.inputlocation.PathBasedAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag("Java8")
public class DeadAssignmentEliminatorTest {

  Path classFilePath =
      Paths.get("../shared-test-resources/bugfixes/DeadAssignmentEliminatorTest.class");

  /**
   *
   *
   * <pre>
   *     void test() {
   *       if (10 < 20) {
   *         int a = 42;
   *       }
   *       return;
   *     }
   * </pre>
   *
   * gets simplified to
   *
   * <pre>
   *     void test() {
   *       if (10 < 20) {
   *       }
   *       return;
   *     }
   * </pre>
   *
   * There used to be a bug that would result in an invalid statement graph because the whole block
   * containing `int a = 42;` gets deleted.
   */
  @Test
  public void conditionalToRemovedBlock() {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Set<Local> locals = Collections.singleton(a);

    BranchingStmt conditional =
        JavaJimple.newIfStmt(
            JavaJimple.newLtExpr(IntConstant.getInstance(10), IntConstant.getInstance(20)),
            noPositionInfo);
    Stmt ret = JavaJimple.newReturnVoidStmt(noPositionInfo);
    FallsThroughStmt intToA =
        JavaJimple.newAssignStmt(a, IntConstant.getInstance(42), noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.setLocals(locals);
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();

    stmtGraph.setStartingStmt(conditional);
    stmtGraph.putEdge(conditional, JIfStmt.FALSE_BRANCH_IDX, intToA);
    stmtGraph.putEdge(conditional, JIfStmt.TRUE_BRANCH_IDX, ret);
    stmtGraph.putEdge(intToA, ret);

    Body beforeBody = builder.build();
    builder = Body.builder(beforeBody, Collections.emptySet());
    new DeadAssignmentEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body afterBody = builder.build();

    assertEquals(
        beforeBody.getStmtGraph().getNodes().size() - 1,
        afterBody.getStmtGraph().getNodes().size());
  }

  @Test
  public void testRemoveDeadAssignment() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();

    Body.BodyBuilder builder = Body.builder(testBody, Collections.emptySet());
    new DeadAssignmentEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body processedBody = builder.build();

    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size() - 1, actualGraph.getNodes().size());
  }

  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator()
        .interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();
    StmtGraph<?> expectedGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.getNodes().size(), actualGraph.getNodes().size());
  }

  private static Body.BodyBuilder createBody(boolean essentialOption) {
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType objectType = JavaIdentifierFactory.getInstance().getClassType("java.lang.Object");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", objectType);
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    FallsThroughStmt strToA =
        JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(a, noPositionInfo);

    Set<Local> locals = new LinkedHashSet<>(Arrays.asList(a, b, c));

    Body.BodyBuilder builder = Body.builder();
    final MutableStmtGraph stmtGraph = builder.getStmtGraph();
    stmtGraph.setStartingStmt(strToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));

    if (essentialOption) {
      FallsThroughStmt newToB =
          JavaJimple.newAssignStmt(b, JavaJimple.newNewExpr(objectType), noPositionInfo);
      stmtGraph.putEdge(strToA, newToB);
      stmtGraph.putEdge(newToB, ret);
    } else {
      FallsThroughStmt intToC =
          JavaJimple.newAssignStmt(c, IntConstant.getInstance(42), noPositionInfo);
      stmtGraph.putEdge(strToA, intToC);
      stmtGraph.putEdge(intToC, ret);
    }
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }

  @Test
  public void testDeadAssignmentEliminator() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            classFilePath,
            "",
            SourceType.Application,
            Collections.singletonList(new DeadAssignmentEliminator()));
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "DeadAssignmentEliminatorTest", "tc1", "void", Collections.emptyList());
    Body body = view.getMethod(methodSignature).get().getBody();
    assertFalse(body.getStmts().isEmpty());
    assertEquals(
        Stream.of(
                "DeadAssignmentEliminatorTest this",
                "unknown $stack3, $stack4, $stack5, l1, l2",
                "this := @this: DeadAssignmentEliminatorTest",
                "l1 = 30",
                "l2 = l1",
                "if l2 <= 5 goto label1",
                "l1 = 40",
                "$stack5 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack5.<java.io.PrintStream: void println(int)>(l1)",
                "label1:",
                "$stack3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack3.<java.io.PrintStream: void println(int)>(l1)",
                "l2 = 30",
                "$stack4 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack4.<java.io.PrintStream: void println(int)>(l2)",
                "return")
            .collect(Collectors.toList()),
        Utils.filterJimple(body.toString()));

    final MethodSignature methodSignature1 =
        view.getIdentifierFactory()
            .getMethodSignature(
                "DeadAssignmentEliminatorTest", "tc2", "void", Collections.emptyList());
    Body body1 = view.getMethod(methodSignature1).get().getBody();
    assertFalse(body1.getStmts().isEmpty());
    assertEquals(
        Stream.of(
                "DeadAssignmentEliminatorTest this",
                "unknown $stack2, $stack3, l1",
                "this := @this: DeadAssignmentEliminatorTest",
                "l1 = \"cde\"",
                "$stack2 = virtualinvoke l1.<java.lang.String: int length()>()",
                "if $stack2 <= 2 goto label1",
                "l1 = \"if\"",
                "label1:",
                "$stack3 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $stack3.<java.io.PrintStream: void println(java.lang.String)>(l1)",
                "return")
            .collect(Collectors.toList()),
        Utils.filterJimple(body1.toString()));
  }

  @Test
  public void testLocalCountAfterDAE() {
    AnalysisInputLocation inputLocation =
        new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(
            classFilePath,
            "",
            SourceType.Application,
            Arrays.asList(new DeadAssignmentEliminator(), new LocalPacker()));
    JavaView view = new JavaView(Collections.singletonList(inputLocation));
    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                "DeadAssignmentEliminatorTest",
                "tc3",
                "void",
                Collections.singletonList(PrimitiveType.getInt().getName()));

    Body body = view.getMethod(methodSignature).get().getBody();
    assertTrue(body.getLocals().size() == 5);
    assertFalse(body.getStmts().isEmpty());
    assertEquals(
        Stream.of(
                "DeadAssignmentEliminatorTest this0",
                "int l1",
                "unknown $stack2, l3, l4",
                "this0 := @this: DeadAssignmentEliminatorTest",
                "l1 := @parameter0: int",
                "label1:",
                "$stack2 = \"true\"",
                "l3 = staticinvoke <java.lang.System: java.lang.String getProperty(java.lang.String)>(\"com.fasterxml.jackson.core.util.BufferRecyclers.trackReusableBuffers\")",
                "l4 = virtualinvoke $stack2.<java.lang.String: boolean equals(java.lang.Object)>(l3)",
                "label2:",
                "goto label4",
                "label3:",
                "l3 := @caughtexception",
                "label4:",
                "if l4 == 0 goto label5",
                "l3 = staticinvoke <java.lang.Boolean: java.lang.Boolean valueOf(boolean)>(1)",
                "goto label6",
                "label5:",
                "l3 = null",
                "label6:",
                "l3 = virtualinvoke l3.<java.lang.Boolean: boolean booleanValue()>()",
                "return",
                "catch java.lang.SecurityException from label1 to label2 with label3")
            .collect(Collectors.toList()),
        Utils.filterJimple(body.toString()));
  }
}
