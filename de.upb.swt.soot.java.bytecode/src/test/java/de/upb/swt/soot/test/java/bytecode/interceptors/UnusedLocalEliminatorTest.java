package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.UnusedLocalEliminator;
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
public class UnusedLocalEliminatorTest {

  @Test
  public void testNoInput() {
    Set<Local> locals = Collections.emptySet();
    List<Trap> traps = Collections.emptyList();
    List<Stmt> stmts = Collections.emptyList();
    Body testBody = Body.getNoBody(); // FIXME [ms] = new Body(locals, traps, stmts, null);
    Body processedBody = new UnusedLocalEliminator().interceptBody(testBody);

    assertNotNull(processedBody);
    assertArrayEquals(testBody.getStmts().toArray(), processedBody.getStmts().toArray());
  }

  @Test
  public void testRemoveUnusedDefsAndUses() {
    Body originalBody = createBody(true);
    Body processedBody = new UnusedLocalEliminator().interceptBody(originalBody);

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
    Body originalBody = createBody(false);
    Body processedBody = new UnusedLocalEliminator().interceptBody(originalBody);

    assertArrayEquals(originalBody.getStmts().toArray(), processedBody.getStmts().toArray());
  }

  private static Body createBody(boolean unusedLocals) {
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

    List<Trap> traps = new ArrayList<>();
    List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);

    return Body.getNoBody(); // FIXME [ms] new Body(locals, traps, stmts, null);
  }
}
