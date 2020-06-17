package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.graph.ImmutableGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.NopEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NopEliminatorTest {

  /** Tests the correct handling of an empty {@link Body}. */
  @Test
  public void testNoInput() {
    Body testBody = Body.getNoBody();
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertNotNull(processedBody);
    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList. It should be deleted.
   */
  @Ignore
  public void testJNopEnd() {
    Body testBody = createBody(true, false);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    ImmutableGraph<Stmt> expectedGraph = testBody.getStmtGraph();
    ImmutableGraph<Stmt> actualGraph = processedBody.getStmtGraph();

    System.out.println(testBody);

    System.out.println(processedBody);

    assertEquals(expectedGraph.nodes().size() - 1, actualGraph.nodes().size());
  }

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList, which also is a Trap.
   * It should not be deleted.
   */
  @Test
  public void testJNopEndTrap() {
    Body testBody = createBody(true, true);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /** Tests the correct handling of a body without nops. */
  @Test
  public void testNoJNops() {
    Body testBody = createBody(false, false);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertEquals(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param withNop indicates, whether a nop is included
   * @param withTrap indicates, whether a trap is included
   * @return the generated {@link Body}
   */
  private static Body createBody(boolean withNop, boolean withTrap) {
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

    Stmt handler = JavaJimple.newReturnStmt(b, noPositionInfo);

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
      if (withTrap) {
        ClassType throwable = factory.getClassType("java.lang.Throwable");
        Trap trap = Jimple.newTrap(throwable, strToA, nop, handler);
        traps.add(trap);
      }
      builder.addFlow(nop, ret); // [ms] wuite artificial flow

    } else {
      stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);
      stmts.forEach(stmt -> builder.addStmt(stmt, true));
    }
    builder.addFlow(jump, ret);

    builder.setLocals(locals);
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder.build();
  }
}
