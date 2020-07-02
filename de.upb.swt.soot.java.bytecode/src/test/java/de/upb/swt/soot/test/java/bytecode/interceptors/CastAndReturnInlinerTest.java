package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.CastAndReturnInliner;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class CastAndReturnInlinerTest {

  /**
   * Tests the transformation from
   *
   * <pre>
   * a = "str";
   * goto l0;
   * l0: b = (String) a;
   * return b;
   * </pre>
   *
   * to
   *
   * <pre>
   * a = "str";
   * return a; // This has changed
   * l0: b = (String) a;
   * return b;
   * </pre>
   */
  @Test
  public void testModification() {
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
    List<Trap> traps = Collections.emptyList();

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    bodyBuilder.setTraps(traps);
    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, jump);
    bodyBuilder.addFlow(jump, bToA);
    bodyBuilder.addFlow(bToA, ret);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    Body testBody = bodyBuilder.build();

    Body processedBody = new CastAndReturnInliner().interceptBody(testBody);

    Set<Stmt> expected = new HashSet<>();
    expected.add(strToA);
    expected.add(bToA);
    expected.add(JavaJimple.newReturnStmt(a, noPositionInfo));
    expected.add(ret);
    assertStmtsEquiv(expected, processedBody.getStmtGraph().nodes());
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
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);
    Local c = JavaJimple.newLocal("c", stringType);

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt strToC = JavaJimple.newAssignStmt(c, javaJimple.newStringConstant("str2"), noPositionInfo);
    Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    // Note this returns c, not b, hence the cast and return must not be inlined
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);
    Stmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);
    List<Trap> traps = Collections.emptyList();

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    bodyBuilder.setTraps(traps);
    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, strToC);
    bodyBuilder.addFlow(strToC, jump);
    bodyBuilder.addFlow(jump, bToA);
    bodyBuilder.addFlow(bToA, ret);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    Body testBody = bodyBuilder.build();

    Body processedBody = new CastAndReturnInliner().interceptBody(testBody);

    assertStmtsEquiv(testBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  private static void assertStmtsEquiv(Set<Stmt> expected, Set<Stmt> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
  }
}
