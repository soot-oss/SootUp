package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.java.bytecode.interceptors.NopEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;

import java.util.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;

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

    new NopEliminator().interceptBody(builder);
    Body processedBody = builder.build();

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
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new NopEliminator().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();

    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
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

    Set<Local> locals = new LinkedHashSet<>();
    locals.add(a);
    locals.add(b);
    List<Trap> traps = new ArrayList<>();

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
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
