package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.graph.*;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  // JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType booleanType = factory.getClassType("boolean");

  // build locals
  Local i0 = JavaJimple.newLocal("$i0", intType);
  Local i1 = JavaJimple.newLocal("$i1", intType);
  Local i2 = JavaJimple.newLocal("$i2", intType);
  Local i3 = JavaJimple.newLocal("$i3", intType);
  Local z0 = JavaJimple.newLocal("$z0", booleanType);
  Local i0hash0 = JavaJimple.newLocal("$i0#0", intType);
  Local i0hash1 = JavaJimple.newLocal("$i0#1", intType);

  // build Stmts
  Stmt stmt1 = JavaJimple.newAssignStmt(i0, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt2 =
      JavaJimple.newAssignStmt(
          z0, JavaJimple.newLtExpr(i0, IntConstant.getInstance(10)), noStmtPositionInfo);
  Stmt stmt3 =
      JavaJimple.newIfStmt(
          JavaJimple.newEqExpr(z0, IntConstant.getInstance(0)), noStmtPositionInfo); // goto stmt10
  Stmt stmt4 =
      JavaJimple.newAssignStmt(
          i1, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt5 = JavaJimple.newAssignStmt(i0, i1, noStmtPositionInfo);
  Stmt stmt6 = JavaJimple.newAssignStmt(i2, i0, noStmtPositionInfo);
  Stmt stmt7 =
      JavaJimple.newAssignStmt(
          i3, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt8 = JavaJimple.newAssignStmt(i0, i3, noStmtPositionInfo);
  Stmt stmt9 = JavaJimple.newGotoStmt(noStmtPositionInfo); // goto stmt2
  Stmt stmt10 = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  Stmt stmt1_m = JavaJimple.newAssignStmt(i0hash0, IntConstant.getInstance(0), noStmtPositionInfo);
  Stmt stmt2_m =
      JavaJimple.newAssignStmt(
          z0, JavaJimple.newLtExpr(i0hash0, IntConstant.getInstance(10)), noStmtPositionInfo);
  Stmt stmt4_m =
      JavaJimple.newAssignStmt(
          i1, JavaJimple.newAddExpr(i0hash0, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt5_m = JavaJimple.newAssignStmt(i0hash1, i1, noStmtPositionInfo);
  Stmt stmt6_m = JavaJimple.newAssignStmt(i2, i0hash1, noStmtPositionInfo);
  Stmt stmt7_m =
      JavaJimple.newAssignStmt(
          i3, JavaJimple.newAddExpr(i0hash1, IntConstant.getInstance(1)), noStmtPositionInfo);
  Stmt stmt8_m = JavaJimple.newAssignStmt(i0hash0, i3, noStmtPositionInfo);

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   * 1. $i0 = 0
   * 2. $z0 = $i0 < 10
   * 3. if $z0 == 0 goto return
   * 4. $i1 = $i0 + 1
   * 5. $i0 = $i1
   * 6. $i2 = $i0
   * 7. $i3 = $i0 + 1
   * 8. $i0 = $i3
   * 9. goto [?= $z0 = $i0 < 10]
   * 10. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * $i0#0 = 0
   * $z0 = $i0#0 < 10
   * if $z0 == 0 goto return
   * $i1 = $i0#0 + 1
   * $i0#1 = $i1
   * $i2 = $i0#1
   * $i3 = $i0#1 + 1
   * $i0#0 = $i3
   * goto [?= $z0 = $i0#0 < 10]
   * return
   * </pre>
   */
  @Test
  public void testLocalSplitter1() {

    Body body = createBody1();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedBody1();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }

  /** bodycreater 1 */
  private Body createBody1() {

    // build set locals
    Set<Local> locals = new HashSet<>();
    locals.add(i0);
    locals.add(i1);
    locals.add(i2);
    locals.add(i3);
    locals.add(z0);

    // build traps (an empty list)
    List<Trap> traps = Collections.emptyList();

    // build stmtGraph
    MutableGraph<Stmt> stmtGraph =
        GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    // set nodes
    stmtGraph.addNode(stmt1);
    stmtGraph.addNode(stmt2);
    stmtGraph.addNode(stmt3);
    stmtGraph.addNode(stmt4);
    stmtGraph.addNode(stmt5);
    stmtGraph.addNode(stmt6);
    stmtGraph.addNode(stmt7);
    stmtGraph.addNode(stmt8);
    stmtGraph.addNode(stmt9);
    stmtGraph.addNode(stmt10);
    // set edges
    stmtGraph.putEdge(stmt1, stmt2);
    stmtGraph.putEdge(stmt2, stmt3);
    stmtGraph.putEdge(stmt3, stmt4);
    stmtGraph.putEdge(stmt3, stmt10);
    stmtGraph.putEdge(stmt4, stmt5);
    stmtGraph.putEdge(stmt5, stmt6);
    stmtGraph.putEdge(stmt6, stmt7);
    stmtGraph.putEdge(stmt7, stmt8);
    stmtGraph.putEdge(stmt8, stmt9);
    stmtGraph.putEdge(stmt9, stmt2);

    // build the map branches
    Map<Stmt, List<Stmt>> branches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(stmt10);
    branches1.add(stmt4);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(stmt2);
    branches.put(stmt3, branches1);
    branches.put(stmt9, branches2);

    // build startingStmt
    Stmt startingStmt = stmt1;

    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(locals, traps, stmtGraph, branches, startingStmt, position);
  }

  private Body createExpectedBody1() {
    // build expected set locals
    Set<Local> expectedLocals = new HashSet<>();
    expectedLocals.add(i1);
    expectedLocals.add(i2);
    expectedLocals.add(i3);
    expectedLocals.add(z0);
    expectedLocals.add(i0hash0);
    expectedLocals.add(i0hash1);

    // build expected traps (an empty list)
    List<Trap> expectedTraps = Collections.emptyList();

    // build expected stmtGraph
    MutableGraph<Stmt> expectedStmtGraph =
        GraphBuilder.directed().nodeOrder(ElementOrder.insertion()).build();
    expectedStmtGraph.addNode(stmt1_m);
    expectedStmtGraph.addNode(stmt2_m);
    expectedStmtGraph.addNode(stmt3);
    expectedStmtGraph.addNode(stmt4_m);
    expectedStmtGraph.addNode(stmt5_m);
    expectedStmtGraph.addNode(stmt6_m);
    expectedStmtGraph.addNode(stmt7_m);
    expectedStmtGraph.addNode(stmt8_m);
    expectedStmtGraph.addNode(stmt9);
    expectedStmtGraph.addNode(stmt10);
    expectedStmtGraph.putEdge(stmt1_m, stmt2_m);
    expectedStmtGraph.putEdge(stmt2_m, stmt3);
    expectedStmtGraph.putEdge(stmt3, stmt4_m);
    expectedStmtGraph.putEdge(stmt3, stmt10);
    expectedStmtGraph.putEdge(stmt4_m, stmt5_m);
    expectedStmtGraph.putEdge(stmt5_m, stmt6_m);
    expectedStmtGraph.putEdge(stmt6_m, stmt7_m);
    expectedStmtGraph.putEdge(stmt7_m, stmt8_m);
    expectedStmtGraph.putEdge(stmt8_m, stmt9);
    expectedStmtGraph.putEdge(stmt9, stmt2_m);

    // build the expected map branches
    Map<Stmt, List<Stmt>> expectedBranches = new HashMap<>();
    List<Stmt> branches1 = new ArrayList<>();
    branches1.add(stmt10);
    branches1.add(stmt4_m);
    List<Stmt> branches2 = new ArrayList<>();
    branches2.add(stmt2_m);
    expectedBranches.put(stmt3, branches1);
    expectedBranches.put(stmt9, branches2);

    // build the expected firstStmt
    Stmt expectedFirstStmt = stmt1_m;
    // build position
    Position position = NoPositionInformation.getInstance();

    return new Body(
        expectedLocals,
        expectedTraps,
        expectedStmtGraph,
        expectedBranches,
        expectedFirstStmt,
        position);
  }

  private static void assertLocalsEquiv(Set<Local> expected, Set<Local> actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    assertEquals(expected.size(), actual.size());
    boolean isEqual = true;
    for (Local local : actual) {
      if (!expected.contains(local)) {
        isEqual = false;
        break;
      }
    }
    assertTrue(isEqual);
  }

  private static void assertStmtGraphEquiv(Graph<Stmt> expected, Graph<Stmt> actual) {
    assertNotNull(expected);
    assertNotNull(actual);

    Set<Stmt> actualStmts = actual.nodes();
    Set<Stmt> expectedStmts = expected.nodes();
    assertEquals(expectedStmts.size(), actualStmts.size());

    Set<EndpointPair<Stmt>> actualEdges = actual.edges();
    Set<EndpointPair<Stmt>> expectedEdges = expected.edges();
    assertEquals(expectedEdges.size(), actualEdges.size());

    boolean isEqual = true;
    boolean isContains = false;
    for (Stmt stmt : actualStmts) {
      for (Stmt expectedStmt : expectedStmts) {
        if (stmt.equivTo(expectedStmt)) {
          isContains = true;
        }
      }
      if (!isContains) {
        isEqual = false;
        break;
      }
      isContains = false;
    }

    if (isEqual) {
      isContains = false;
      for (EndpointPair<Stmt> edge : actualEdges) {
        Stmt souce = edge.source();
        Stmt target = edge.target();
        for (EndpointPair<Stmt> expectedEdge : expectedEdges) {
          Stmt expectedSouce = edge.source();
          Stmt expectedTarget = edge.target();
          if (souce.equivTo(expectedSouce) && target.equivTo(expectedTarget)) {
            isContains = true;
          }
        }
        if (!isContains) {
          isEqual = false;
          break;
        }
        isContains = false;
      }
    }
    assertTrue(isEqual);
  }
}
