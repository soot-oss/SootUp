package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.Jimple;
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
    Set<Local> locals = Collections.emptySet();
    List<Trap> traps = Collections.emptyList();
    List<Stmt> stmts = Collections.emptyList();
    Body testBody = new Body(locals, traps, stmts, null);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertNotNull(processedBody);
    assertArrayEquals(testBody.getStmts().toArray(), processedBody.getStmts().toArray());
  }

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList. It should be deleted.
   */
  @Test
  public void testJNopEnd() {
    Body testBody = createBody(true, false);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    List<Stmt> expectedList = testBody.getStmts();
    List<Stmt> actualList = processedBody.getStmts();
    assertEquals(expectedList.size() - 1, actualList.size());
    for (int i = 0; i < testBody.getStmts().size() - 1; i++) {
      assertSame(expectedList.get(i), actualList.get(i));
    }
  }

  /**
   * Tests the correct handling of a nop statement at the end of the stmtList, which also is a Trap.
   * It should not be deleted.
   */
  @Test
  public void testJNopEndTrap() {
    Body testBody = createBody(true, true);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertArrayEquals(testBody.getStmts().toArray(), processedBody.getStmts().toArray());
  }

  /** Tests the correct handling of a body without nops. */
  @Test
  public void testNoJNops() {
    Body testBody = createBody(false, false);
    Body processedBody = new NopEliminator().interceptBody(testBody);

    assertArrayEquals(testBody.getStmts().toArray(), processedBody.getStmts().toArray());
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
    Stmt jump = JavaJimple.newGotoStmt(bToA, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b);
    List<Trap> traps = new ArrayList<>();
    List<Stmt> stmts;

    if (withNop) {
      JNopStmt nop = new JNopStmt(noPositionInfo);
      stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret, nop);
      if (withTrap) {
        Trap trap = Jimple.newTrap(null, null, new JStmtBox(nop), null);
        traps.add(trap);
      }
    } else {
      stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);
    }

    return new Body(locals, traps, stmts, null);
  }
}
