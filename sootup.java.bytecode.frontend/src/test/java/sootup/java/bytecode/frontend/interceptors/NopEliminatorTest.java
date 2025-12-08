package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.util.ImmutableUtils;
import sootup.interceptors.NopEliminator;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Marcus Nachtigall
 */
public class NopEliminatorTest {

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList. It should be deleted.
   * Transforms from
   *
   * <p>a = "str"; goto label1; label1: b = (java.lang.String) a; nop; return b;
   *
   * <p>to
   *
   * <p>a = "str"; goto label1; label1: b = (java.lang.String) a; return b;
   */
  @Test
  public void testJNopEnd() {
    Body.BodyBuilder builder = createBody(true);
    Body testBody = builder.build();

    builder = Body.builder(testBody, builder.getModifiers());
    new NopEliminator().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body processedBody = builder.build();

    ControlFlowGraph<?> inputControlFlowGraph = testBody.getControlFlowGraph();
    ControlFlowGraph<?> actualGraph = processedBody.getControlFlowGraph();

    assertEquals(inputControlFlowGraph.getNodes().size() - 1, actualGraph.getNodes().size());
  }

  /**
   * Tests the correct handling of a body without nops. Considers the following:
   *
   * <p>a = "str"; goto label1; b = (java.lang.String) a; label1: return b;
   *
   * <p>Does not change anything.
   */
  @Test
  public void testNoJNops() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new NopEliminator().interceptBody(testBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = testBuilder.build();

    assertEquals(
        testBody.getControlFlowGraph().getNodes(), processedBody.getControlFlowGraph().getNodes());
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param withNop indicates, whether a nop is included
   * @return the generated {@link Body}
   */
  private static Body.BodyBuilder createBody(boolean withNop) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);

    FallsThroughStmt strToA =
        JavaJimple.newAssignStmt(a, JavaJimple.newStringConstant("str"), noPositionInfo);
    FallsThroughStmt bToA =
        JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    BranchingStmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));

    final MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
    controlFlowGraph.setStartingStmt(strToA);
    controlFlowGraph.putEdge(strToA, jump);
    controlFlowGraph.putEdge(jump, JGotoStmt.BRANCH_IDX, bToA);
    controlFlowGraph.putEdge(bToA, ret);
    if (withNop) {
      // strToA, jump, bToA, nop, ret;
      JNopStmt nop = new JNopStmt(noPositionInfo);
      controlFlowGraph.removeEdge(bToA, ret);
      controlFlowGraph.putEdge(bToA, nop);
      controlFlowGraph.putEdge(nop, ret);
    }
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
