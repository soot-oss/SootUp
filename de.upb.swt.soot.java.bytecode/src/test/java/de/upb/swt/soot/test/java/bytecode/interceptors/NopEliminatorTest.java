package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.NopEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NopEliminatorTest {

  /** Tests the correct handling of an empty {@link Body}. */
  @Test
  public void testNoInput() {
    Body testBody = Body.builder().build();
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertNotNull(processedBody);
    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList. It should be deleted.
   * Transforms from
   *
   * <p>a = "str"; goto label1; b = (java.lang.String) a; label1: return b; nop;
   *
   * <p>to
   *
   * <p>a = "str"; goto label1; b = (java.lang.String) a; label1: return b;
   */
  @Test
  public void testJNopEnd() {
    Body testBody = createBody(true);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    ImmutableStmtGraph expectedGraph = testBody.getStmtGraph();
    ImmutableStmtGraph actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.nodes().size() - 1, actualGraph.nodes().size());
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
    Body testBody = createBody(false);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param withNop indicates, whether a nop is included
   * @return the generated {@link Body}
   */
  private static Body createBody(boolean withNop) {
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
    List<Trap> traps = new ArrayList<>();
    List<Stmt> stmts;

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    if (withNop) {
      JNopStmt nop = new JNopStmt(noPositionInfo);
      stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret, nop);
      builder.addStmts(stmts, true);
      builder.addFlow(nop, ret);
    } else {
      stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);
      builder.addStmts(stmts, true);
    }
    builder.addFlow(jump, ret);
    builder.setLocals(locals);
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
