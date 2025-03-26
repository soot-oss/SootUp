package sootup.java.bytecode.frontend.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.UnknownType;
import sootup.interceptors.CastAndReturnInliner;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Marcus Nachtigall
 */
public class CastAndReturnInlinerTest {

  /**
   * Tests the transformation from
   *
   * <pre>
   * a = "str";
   * goto label0;
   * ...
   * label0:
   * b = (String) a;
   * return b;
   * </pre>
   *
   * to
   *
   * <pre>
   * a_ret = (String) "str";
   * return a_ret0;
   * </pre>
   */
  @Test
  public void testModification() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);

    FallsThroughStmt strToA =
        JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    FallsThroughStmt bToA =
        JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    BranchingStmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    Set<Local> locals = new HashSet<>(Arrays.asList(a, b));

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);

    final MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(strToA);
    stmtGraph.putEdge(strToA, jump);
    stmtGraph.putEdge(jump, JGotoStmt.BRANCH_IDX, bToA);
    stmtGraph.putEdge(bToA, ret);

    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));

    new CastAndReturnInliner().interceptBody(bodyBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = bodyBuilder.build();

    List<Stmt> expected = new ArrayList<>();
    expected.add(strToA);
    JavaLocal aRet = JavaJimple.newLocal("a_ret0", stringType);
    expected.add(
        JavaJimple.newAssignStmt(
            aRet, JavaJimple.newCastExpr(a, stringType), StmtPositionInfo.getNoStmtPositionInfo()));
    expected.add(JavaJimple.newReturnStmt(aRet, noPositionInfo));
    assertStmtsEquiv(expected, processedBody.getStmts());
    assertEquals(2, processedBody.getLocals().size());
    assertTrue(processedBody.getLocals().contains(new Local("a", UnknownType.getInstance())));
    assertTrue(processedBody.getLocals().contains(new Local("a_ret0", UnknownType.getInstance())));
  }

  /**
   * Tests that the following body is not modified, as it is not eligible for inlining: *
   *
   * <pre>
   * a = "str";
   * c = "str2";
   * goto l0;
   * l0: b = (String) a;
   * return c; // Note that this does not return b
   * </pre>
   */
  @Test
  public void testNoModification() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.getNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);
    Local c = JavaJimple.newLocal("c", stringType);

    FallsThroughStmt strToA =
        JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    FallsThroughStmt strToC =
        JavaJimple.newAssignStmt(c, javaJimple.newStringConstant("str2"), noPositionInfo);
    FallsThroughStmt bToA =
        JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    // Note this returns c, not b, hence the cast and return must not be inlined
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);
    BranchingStmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    Set<Local> locals = new HashSet<>(Arrays.asList(a, b));

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    final MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(strToA);
    stmtGraph.putEdge(strToA, strToC);
    stmtGraph.putEdge(strToC, jump);
    stmtGraph.putEdge(jump, JGotoStmt.BRANCH_IDX, bToA);
    stmtGraph.putEdge(bToA, ret);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("ab.c", "test", "void", Collections.emptyList()));
    Body testBody = bodyBuilder.build();

    new CastAndReturnInliner().interceptBody(bodyBuilder, new JavaView(Collections.emptyList()));
    Body processedBody = bodyBuilder.build();

    assertStmtsEquiv(testBody.getStmts(), processedBody.getStmts());
  }

  private static void assertStmtsEquiv(List<Stmt> expected, List<Stmt> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      boolean condition = expected.get(i).equivTo(actual.get(i));
      if (!condition) {
        System.out.println(expected.get(i) + " <> " + actual.get(i));
      }
      assertTrue(condition);
    }
  }
}
