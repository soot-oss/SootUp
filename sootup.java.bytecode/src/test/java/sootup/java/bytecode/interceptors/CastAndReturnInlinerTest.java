package sootup.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
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
   * a = "str";
   * return a;
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

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);

    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, jump);
    bodyBuilder.addFlow(jump, bToA);
    bodyBuilder.addFlow(bToA, ret);

    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    Body testBody = bodyBuilder.build();

    new CastAndReturnInliner().interceptBody(bodyBuilder, null);
    Body processedBody = bodyBuilder.build();

    List<Stmt> expected = new ArrayList<>();
    expected.add(strToA);
    expected.add(JavaJimple.newReturnStmt(a, noPositionInfo));
    assertStmtsEquiv(expected, processedBody.getStmts());
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

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, strToC);
    bodyBuilder.addFlow(strToC, jump);
    bodyBuilder.addFlow(jump, bToA);
    bodyBuilder.addFlow(bToA, ret);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    Body testBody = bodyBuilder.build();

    new CastAndReturnInliner().interceptBody(bodyBuilder, null);
    Body processedBody = bodyBuilder.build();

    assertStmtsEquiv(testBody.getStmts(), processedBody.getStmts());
  }

  private static void assertStmtsEquiv(List<Stmt> expected, List<Stmt> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      assertTrue(expected.get(i).equivTo(actual.get(i)));
    }
  }
}
