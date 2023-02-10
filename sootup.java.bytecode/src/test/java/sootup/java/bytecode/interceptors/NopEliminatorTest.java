package sootup.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
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

    new NopEliminator().interceptBody(builder, null);
    Body processedBody = builder.build();

    StmtGraph<?> inputStmtGraph = testBody.getStmtGraph();
    StmtGraph<?> actualGraph = processedBody.getStmtGraph();

    assertEquals(inputStmtGraph.getNodes().size() - 1, actualGraph.getNodes().size());
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
    new NopEliminator().interceptBody(testBuilder, null);
    Body processedBody = testBuilder.build();

    assertEquals(testBody.getStmtGraph().getNodes(), processedBody.getStmtGraph().getNodes());
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param withNop indicates, whether a nop is included
   * @return the generated {@link Body}
   */
  private static Body.BodyBuilder createBody(boolean withNop) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    Stmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(strToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.addFlow(strToA, jump);
    builder.addFlow(jump, bToA);
    builder.addFlow(bToA, ret);
    if (withNop) {
      // strToA, jump, bToA, nop, ret;
      JNopStmt nop = new JNopStmt(noPositionInfo);
      builder.removeFlow(bToA, ret);
      builder.addFlow(bToA, nop);
      builder.addFlow(nop, ret);
    }
    builder.setLocals(locals);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
