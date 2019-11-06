package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class CastAndReturnInlinerTest {

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
    Stmt jump = JavaJimple.newGotoStmt(bToA, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);
    List<Trap> traps = Collections.emptyList();
    List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);
    Body testBody = new Body(locals, traps, stmts, null);

    Body processedBody = new CastAndReturnInliner().interceptBody(testBody);

    assertStmtsEquiv(
        ImmutableUtils.immutableList(
            strToA, JavaJimple.newReturnStmt(a, noPositionInfo), bToA, ret),
        processedBody.getStmts());
  }

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
    Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    // Note this returns c, not b, hence the cast and return must not be inlined
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);
    Stmt jump = JavaJimple.newGotoStmt(bToA, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);
    List<Trap> traps = Collections.emptyList();
    List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);
    Body testBody = new Body(locals, traps, stmts, null);

    Body processedBody = new CastAndReturnInliner().interceptBody(testBody);

    assertStmtsEquiv(testBody.getStmts(), processedBody.getStmts());
  }

  private static void assertStmtsEquiv(List<Stmt> expected, List<Stmt> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());

    for (int i = 0; i < expected.size(); i++) {
      assertTrue(expected.get(i).equivTo(actual.get(i)));
    }
  }
}
