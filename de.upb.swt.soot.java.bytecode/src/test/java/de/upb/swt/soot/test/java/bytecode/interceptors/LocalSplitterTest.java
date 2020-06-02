package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   * $i0 = 0
   * $z0 = $i0 < 10
   * if $z0 == 0 goto return
   * $i1 = $i0 + 1
   * $i0 = $i1
   * $i2 = $i0
   * $i3 = $i0 + 1
   * $i0 = $i3
   * goto [?= $z0 = $i0 < 10]
   *  return
   * </pre>
   *
   * to:
   *
   * <pre>
   * $i0 = 0
   * $z0 = $i0 < 10
   * if $z0 == 0 goto return
   * $i1 = $i0 + 1
   * $i0#0 = $i1
   * $i2 = $i0#0
   * $i3 = $i0#0 + 1
   * $i0 = $i3
   * goto [?= $z0 = $i0 < 10]
   * return
   * </pre>
   */
  @Test
  public void testLocalSplitter() {

    /**
     * // build the test body JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
     * JavaJimple javaJimple = JavaJimple.getInstance(); StmtPositionInfo noStmtPositionInfo =
     * StmtPositionInfo.createNoStmtPositionInfo();
     *
     * <p>JavaClassType intType = factory.getClassType("int"); JavaClassType booleanType =
     * factory.getClassType("boolean");
     *
     * <p>Local i0 = JavaJimple.newLocal("$i0", intType); Local i1 = JavaJimple.newLocal("$i1",
     * intType); Local i2 = JavaJimple.newLocal("$i2", intType); Local i3 =
     * JavaJimple.newLocal("$i3", intType); Local z0 = JavaJimple.newLocal("$z0", booleanType);
     *
     * <p>Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo); Stmt stmt1 =
     * JavaJimple.newAssignStmt(i0, IntConstant.getInstance(0), noStmtPositionInfo);
     *
     * <p>Stmt stmt2 = JavaJimple.newAssignStmt( z0, JavaJimple.newLtExpr(i0,
     * IntConstant.getInstance(10)), noStmtPositionInfo); Stmt stmt3 = JavaJimple.newIfStmt(
     * JavaJimple.newEqExpr(z0, IntConstant.getInstance(0)), ret, noStmtPositionInfo); Stmt stmt4 =
     * JavaJimple.newAssignStmt( i1, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)),
     * noStmtPositionInfo); Stmt stmt5 = JavaJimple.newAssignStmt(i0, i1, noStmtPositionInfo); Stmt
     * stmt6 = JavaJimple.newAssignStmt(i2, i0, noStmtPositionInfo); Stmt stmt7 =
     * JavaJimple.newAssignStmt( i3, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)),
     * noStmtPositionInfo); Stmt stmt8 = JavaJimple.newAssignStmt(i0, i3, noStmtPositionInfo); Stmt
     * stmt9 = JavaJimple.newGotoStmt(stmt2, noStmtPositionInfo);
     *
     * <p>Set<Local> locals = ImmutableUtils.immutableSet(i0, i1, i2, i3, z0); List<Trap> traps =
     * Collections.emptyList(); List<Stmt> stmts = ImmutableUtils.immutableList( stmt1, stmt2,
     * stmt3, stmt4, stmt5, stmt6, stmt7, stmt8, stmt9, ret); Body testBody = new Body(locals,
     * traps, stmts, null);
     *
     * <p>LocalSplitter localSplitter = new LocalSplitter(); Body newBody =
     * localSplitter.interceptBody(testBody);
     *
     * <p>/** // Test Local list Local expectedNewLocal = JavaJimple.newLocal("$i0#0", intType);
     * Set<Local> expectedLocals = new HashSet<>(testBody.getLocals());
     * expectedLocals.add(expectedNewLocal);
     *
     * <p>assertEquals(newBody.getLocals(), expectedLocals);
     *
     * <p>// Test body stmt List<String> newBodyStringList = new ArrayList<>(); for (Stmt stmt :
     * newBody.getStmts()) { newBodyStringList.add(stmt.toString()); } List<String>
     * expectedBodyStringList = Stream.of( "$i0 = 0", "$z0 = $i0 < 10", "if $z0 == 0 goto return",
     * "$i1 = $i0 + 1", "$i0#0 = $i1", "$i2 = $i0#0", "$i3 = $i0#0 + 1", "$i0 = $i3", "goto [?= $z0
     * = $i0 < 10]", "return") .collect(Collectors.toCollection(ArrayList::new));
     *
     * <p>assertTrue(newBodyStringList.equals(expectedBodyStringList));
     */
    /**
     * transform:
     *
     * <pre>
     * $i0 = 1
     * $i0 = $i0 + 1
     * $i0 = 1
     * $i0 = $i0 + 1
     * $i0 = 1
     * $i0 = $i0 + 1
     * </pre>
     *
     * to:
     *
     * <pre>
     * $i0 = 1
     * $i0#0 = $i0 + 1
     * $i0#1 = 1
     * $i0#2 = $i0#1 + 1
     * $i0#3 = 1
     * $i0#4 = $i0#3+ 1
     * </pre>
     */
    /**
     * stmt1 = JavaJimple.newAssignStmt(i0, IntConstant.getInstance(1), noStmtPositionInfo); stmt2 =
     * JavaJimple.newAssignStmt( i0, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)),
     * noStmtPositionInfo); stmt3 = JavaJimple.newAssignStmt(i0, IntConstant.getInstance(1),
     * noStmtPositionInfo); stmt4 = JavaJimple.newAssignStmt( i0, JavaJimple.newAddExpr(i0,
     * IntConstant.getInstance(1)), noStmtPositionInfo); stmt5 = JavaJimple.newAssignStmt(i0,
     * IntConstant.getInstance(1), noStmtPositionInfo); stmt6 = JavaJimple.newAssignStmt( i0,
     * JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)), noStmtPositionInfo); locals =
     * ImmutableUtils.immutableSet(i0); stmts = ImmutableUtils.immutableList(stmt1, stmt2, stmt3,
     * stmt4, stmt5, stmt6); testBody = new Body(locals, traps, stmts, null); Body newBody2 =
     * localSplitter.interceptBody(testBody); for (Stmt stmt : newBody2.getStmts()) {
     * System.out.println(stmt); }
     */
  }
}
