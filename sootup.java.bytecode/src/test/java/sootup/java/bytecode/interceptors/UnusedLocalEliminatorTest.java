package sootup.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import java.util.Collections;
import java.util.Set;
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
public class UnusedLocalEliminatorTest {

  @Test
  public void testRemoveUnusedDefsAndUses() {
    Body.BodyBuilder builder = createBody(true);
    Body originalBody = builder.build();

    new UnusedLocalEliminator().interceptBody(builder, null);
    Body processedBody = builder.build();

    Set<Local> originalLocals = originalBody.getLocals();
    Set<Local> processedLocals = processedBody.getLocals();
    JavaClassType objectType = JavaIdentifierFactory.getInstance().getClassType("java.lang.Object");
    JavaClassType stringType = JavaIdentifierFactory.getInstance().getClassType("java.lang.String");

    assertEquals(4, originalLocals.size());
    assertEquals(2, processedLocals.size());
    processedLocals = processedBody.getLocals();
    assertTrue(processedLocals.contains(new Local("a", objectType)));
    assertTrue(processedLocals.contains(new Local("b", stringType)));
  }

  @Test
  public void testRemoveNothing() {
    Body.BodyBuilder builder = createBody(false);
    Body originalBody = builder.build();
    new UnusedLocalEliminator().interceptBody(builder, null);
    Body processedBody = builder.build();

    assertArrayEquals(originalBody.getStmts().toArray(), processedBody.getStmts().toArray());
  }

  private static Body.BodyBuilder createBody(boolean unusedLocals) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);
    Set<Local> locals;
    if (unusedLocals) {
      Local c = JavaJimple.newLocal("c", objectType);
      Local d = JavaJimple.newLocal("d", stringType);
      locals = ImmutableUtils.immutableSet(a, b, c, d);
    } else {
      locals = ImmutableUtils.immutableSet(a, b);
    }

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    Stmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    final Body.BodyBuilder builder = Body.builder();
    locals.forEach(builder::addLocal);

    builder.setStartingStmt(strToA);
    builder.addFlow(strToA, jump);
    builder.addFlow(jump, bToA);
    builder.addFlow(bToA, ret);

    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "a.b.c", "void", Collections.emptyList()));
    return builder;
  }
}
