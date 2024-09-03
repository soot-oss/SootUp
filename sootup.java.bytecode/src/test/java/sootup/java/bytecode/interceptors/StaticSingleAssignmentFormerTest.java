package sootup.java.bytecode.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import categories.TestCategories;
import java.util.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.StaticSingleAssignmentFormer;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/** @author Zun Wang */
@Tag(TestCategories.JAVA_8_CATEGORY)
@Disabled("ms: FIX IT")
public class StaticSingleAssignmentFormerTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();
  JavaJimple javaJimple = JavaJimple.getInstance();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType classType = factory.getClassType("Test");
  JavaClassType refType = factory.getClassType("ref");
  MethodSignature methodSignature =
      new MethodSignature(classType, "test", Collections.emptyList(), VoidType.getInstance());
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  ClassType exception = factory.getClassType("Exception");
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local stack4 = JavaJimple.newLocal("stack4", refType);

  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  Stmt assign1tol1 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt assign1tol2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(1), noStmtPositionInfo);
  Stmt assign0tol3 = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(0), noStmtPositionInfo);
  BranchingStmt ifStmt =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l3, IntConstant.getInstance(100)), noStmtPositionInfo);
  BranchingStmt ifStmt2 =
      JavaJimple.newIfStmt(
          JavaJimple.newLtExpr(l2, IntConstant.getInstance(20)), noStmtPositionInfo);
  Stmt returnStmt = JavaJimple.newReturnStmt(l2, noStmtPositionInfo);
  Stmt assignl1tol2 = JavaJimple.newAssignStmt(l2, l1, noStmtPositionInfo);
  Stmt assignl3plus1tol3 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt assignl3tol2 = JavaJimple.newAssignStmt(l2, l3, noStmtPositionInfo);
  Stmt assignl3plus2tol3 =
      JavaJimple.newAssignStmt(
          l3, JavaJimple.newAddExpr(l3, IntConstant.getInstance(2)), noStmtPositionInfo);
  BranchingStmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);

  FallsThroughStmt handlerStmt =
      JavaJimple.newIdentityStmt(stack4, caughtExceptionRef, noStmtPositionInfo);
  FallsThroughStmt l2eq0 =
      JavaJimple.newAssignStmt(l2, IntConstant.getInstance(0), noStmtPositionInfo);
  BranchingStmt goTo = JavaJimple.newGotoStmt(noStmtPositionInfo);

  @Test
  public void testSSA() {
    StaticSingleAssignmentFormer ssa = new StaticSingleAssignmentFormer();
    Body.BodyBuilder builder = createBody();
    ssa.interceptBody(builder, new JavaView(Collections.emptyList()));

    String expectedBodyString =
        "{\n"
            + "    int l0, l1, l2, l3, l0#0, l1#1, l2#2, l3#3, l3#4, l2#5, l2#6, l3#7, l2#8, l3#9, l3#10, l2#11;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @this: Test;\n"
            + "\n"
            + "    l1#1 = 1;\n"
            + "\n"
            + "    l2#2 = 1;\n"
            + "\n"
            + "    l3#3 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l3#4 = phi(l3#3, l3#10);\n"
            + "\n"
            + "    l2#5 = phi(l2#2, l2#11);\n"
            + "\n"
            + "    if l3#4 < 100 goto label3;\n"
            + "\n"
            + "    if l2#5 < 20 goto label2;\n"
            + "\n"
            + "    l2#8 = l1#1;\n"
            + "\n"
            + "    l3#9 = l3#4 + 1;\n"
            + "\n"
            + "    l3#10 = phi(l3#7, l3#9);\n"
            + "\n"
            + "    l2#11 = phi(l2#6, l2#8);\n"
            + "\n"
            + "    goto label1;\n"
            + "\n"
            + "  label2:\n"
            + "    l2#6 = l3#4;\n"
            + "\n"
            + "    l3#7 = l3#4 + 2;\n"
            + "\n"
            + "  label3:\n"
            + "    return l2#5;\n"
            + "}\n";

    assertEquals(expectedBodyString, builder.build().toString());
  }

  @Test
  public void testTrapedSSA() {
    StaticSingleAssignmentFormer ssa = new StaticSingleAssignmentFormer();
    Body.BodyBuilder builder = createTrapBody();
    ssa.interceptBody(builder, new JavaView(Collections.emptyList()));

    String expectedBodyString =
        "{\n"
            + "    int l0, l1, l2, l3, l0#0, l1#1, l2#2, l3#3, l3#4, l2#5, l2#6, l3#7, l2#8, l2#10, l2#11, l3#12, l3#13, l2#14;\n"
            + "    ref stack4, stack4#9;\n"
            + "\n"
            + "\n"
            + "    l0#0 := @this: Test;\n"
            + "\n"
            + "    l1#1 = 1;\n"
            + "\n"
            + "    l2#2 = 1;\n"
            + "\n"
            + "    l3#3 = 0;\n"
            + "\n"
            + "  label1:\n"
            + "    l3#4 = phi(l3#3, l3#13);\n"
            + "\n"
            + "    l2#5 = phi(l2#2, l2#14);\n"
            + "\n"
            + "    if l3#4 < 100 goto label6;\n"
            + "\n"
            + "    if l2#5 < 20 goto label5;\n"
            + "\n"
            + "  label2:\n"
            + "    l2#8 = l1#1;\n"
            + "\n"
            + "  label3:\n"
            + "    l2#11 = phi(l2#8, l2#10);\n"
            + "\n"
            + "    l3#12 = l3#4 + 1;\n"
            + "\n"
            + "    l3#13 = phi(l3#7, l3#12);\n"
            + "\n"
            + "    l2#14 = phi(l2#6, l2#11);\n"
            + "\n"
            + "    goto label1;\n"
            + "\n"
            + "  label4:\n"
            + "    stack4#9 := @caughtexception;\n"
            + "\n"
            + "    l2#10 = 0;\n"
            + "\n"
            + "    goto label3;\n"
            + "\n"
            + "  label5:\n"
            + "    l2#6 = l3#4;\n"
            + "\n"
            + "    l3#7 = l3#4 + 2;\n"
            + "\n"
            + "  label6:\n"
            + "    return l2#5;\n"
            + "\n"
            + " catch Exception from label2 to label3 with label4;\n"
            + "}\n";

    assertEquals(expectedBodyString, builder.build().toString());
  }

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 1
   *    l2 = 1
   *    l3 = 0
   * label1:
   *    if l3 < 100 goto label3
   *    if l2 < 20 goto label 2
   *    l2 = l1
   *    l3 = l3 + 1
   *    goto label1;
   * label2:
   *    l2 = l3
   *    l3 = l3 + 2
   * label3:
   *    return l2
   * </pre>
   */
  private Body.BodyBuilder createBody() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3);
    builder.setLocals(locals);

    Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
    successorMap.put(ifStmt, Collections.singletonList(returnStmt));
    successorMap.put(ifStmt2, Collections.singletonList(assignl1tol2));
    successorMap.put(gotoStmt, Collections.singletonList(ifStmt));

    graph.initializeWith(
        Arrays.asList(
            Arrays.asList(startingStmt, assign1tol1, assign1tol2, assign0tol3, ifStmt),
            Collections.singletonList(ifStmt2),
            Arrays.asList(assignl1tol2, assignl3plus1tol3, gotoStmt),
            Arrays.asList(assignl3tol2, assignl3plus2tol3, returnStmt)),
        successorMap,
        Collections.emptyList());

    return builder;
  }

  /**
   *
   *
   * <pre>
   *    l0 := @this Test
   *    l1 = 1
   *    l2 = 1
   *    l3 = 0
   * label1:
   *    if l3 < 100 goto label6
   *    if l2 < 20 goto label5
   * label2:
   *    l2 = l1
   * label3:
   *    l3 = l3 + 1
   *    goto label1;
   * label4:
   *    stack4 := @caughtexception
   *    l2 = 0;
   *    goto label3;
   * label5:
   *    l2 = l3
   *    l3 = l3 + 2
   * label6:
   *    return l2
   *
   * catch Exception from label2 to label3 with label4;
   * </pre>
   */
  private Body.BodyBuilder createTrapBody() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();
    Body.BodyBuilder builder = Body.builder(graph);
    builder.setMethodSignature(methodSignature);

    // build set locals
    Set<Local> locals = ImmutableUtils.immutableSet(l0, l1, l2, l3, stack4);
    builder.setLocals(locals);
    Map<BranchingStmt, List<Stmt>> successorMap = new HashMap<>();
    successorMap.put(ifStmt, Collections.singletonList(returnStmt));
    successorMap.put(ifStmt2, Collections.singletonList(assignl1tol2));
    successorMap.put(gotoStmt, Collections.singletonList(ifStmt));

    graph.initializeWith(
        Arrays.asList(
            Arrays.asList(startingStmt, assign1tol1, assign1tol2, assign0tol3, ifStmt),
            Collections.singletonList(ifStmt2),
            Arrays.asList(assignl1tol2, assignl3plus1tol3, gotoStmt),
            Arrays.asList(assignl3tol2, assignl3plus2tol3, returnStmt)),
        successorMap,
        Collections.emptyList());

    // add exception
    graph.addNode(assignl1tol2, Collections.singletonMap(exception, handlerStmt));

    graph.putEdge(handlerStmt, l2eq0);
    graph.putEdge(l2eq0, goTo);
    graph.putEdge(goTo, JGotoStmt.BRANCH_IDX, assignl3plus1tol3);

    return builder;
  }
}
