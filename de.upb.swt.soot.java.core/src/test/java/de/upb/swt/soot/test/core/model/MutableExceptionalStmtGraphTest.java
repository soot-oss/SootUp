package de.upb.swt.soot.test.core.model;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.graph.MutableExceptionalStmtGraph;
import de.upb.swt.soot.core.graph.MutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;

public class MutableExceptionalStmtGraphTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
  JavaJimple javaJimple = JavaJimple.getInstance();

  JavaClassType classType = factory.getClassType("Test");
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  JavaClassType refType = factory.getClassType("ref");
  ClassType exception1 = factory.getClassType("Exception1");
  ClassType exception2 = factory.getClassType("Exception2");
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();

  // build locals
  Local l0 = JavaJimple.newLocal("l0", refType);
  Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
  Local l2 = JavaJimple.newLocal("l2", PrimitiveType.getInt());
  Local l3 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local l4 = JavaJimple.newLocal("l4", PrimitiveType.getInt());
  Local stack6 = JavaJimple.newLocal("stack6", refType);
  Local stack7 = JavaJimple.newLocal("stack7", refType);
  Local l8 = JavaJimple.newLocal("l8", PrimitiveType.getInt());
  Local stack9 = JavaJimple.newLocal("stack9", refType);

  // build stmts
  // l0 := @this Test
  Stmt startingStmt = JavaJimple.newIdentityStmt(l0, identityRef, noStmtPositionInfo);
  // l1 = 1;
  Stmt label1Stmt = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(1), noStmtPositionInfo);
  // l2 = 2;
  Stmt label2Stmt = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(2), noStmtPositionInfo);
  // l2 = 3;
  Stmt stmtInLabel2 = JavaJimple.newAssignStmt(l2, IntConstant.getInstance(3), noStmtPositionInfo);
  // l3 = 3
  Stmt label3Stmt = JavaJimple.newAssignStmt(l3, IntConstant.getInstance(3), noStmtPositionInfo);
  // l4 = 4
  Stmt label4Stmt = JavaJimple.newAssignStmt(l4, IntConstant.getInstance(4), noStmtPositionInfo);
  // return
  Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);

  // stack6 := @caughtexception
  Stmt label6Stmt = JavaJimple.newIdentityStmt(stack6, caughtExceptionRef, noStmtPositionInfo);
  // l1 = 0
  Stmt stmtInLabel6 = JavaJimple.newAssignStmt(l1, IntConstant.getInstance(0), noStmtPositionInfo);
  // stack7 := @caughtexception
  Stmt label7Stmt = JavaJimple.newIdentityStmt(stack7, caughtExceptionRef, noStmtPositionInfo);
  // l8 = 8
  Stmt label8Stmt = JavaJimple.newAssignStmt(l8, IntConstant.getInstance(8), noStmtPositionInfo);
  Stmt gotoStmt1 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt gotoStmt2 = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt throwStmt = JavaJimple.newThrowStmt(l1, noStmtPositionInfo);
  // stack9 := @caughtexception
  Stmt label9Stmt = JavaJimple.newIdentityStmt(stack9, caughtExceptionRef, noStmtPositionInfo);

  Trap trap1 = new Trap(exception1, label1Stmt, label4Stmt, label6Stmt);
  Trap trap2 = new Trap(exception1, label2Stmt, label3Stmt, label7Stmt);
  Trap trap3 = new Trap(exception2, label2Stmt, label3Stmt, label7Stmt);
  Trap trap4 = new Trap(exception1, label1Stmt, label3Stmt, label6Stmt);
  Trap trap5 = new Trap(exception1, label6Stmt, stmtInLabel6, label7Stmt);
  Trap trap6 = new Trap(exception1, label7Stmt, label8Stmt, label9Stmt);

  @Test
  public void graphWithNestedTraps_1Test() {
    StmtGraph graph = createGraphWithNestedTraps_1();
    MutableExceptionalStmtGraph exceptionalStmtGraph = new MutableExceptionalStmtGraph(graph);

    Map<Stmt, List<Stmt>> expectedExceptionalPreds = expectedPredsForGraphWithNestedTraps_1();
    Map<Stmt, List<Stmt>> expectedExceptionalSuccs = expectedSuccsForGraphWithNestedTraps_1();
    Map<Stmt, List<Trap>> expectedExceptionalDests = expectedDestsForGraphWithNestedTraps_1();

    for (Stmt stmt : expectedExceptionalPreds.keySet()) {
      assertStmtsListsEquiv(
          expectedExceptionalPreds.get(stmt), exceptionalStmtGraph.exceptionalPredecessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalSuccs.get(stmt), exceptionalStmtGraph.exceptionalSuccessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalDests.get(stmt), exceptionalStmtGraph.getDestTraps(stmt));
    }
  }

  @Test
  public void graphWithNestedTraps_2Test() {
    StmtGraph graph = createGraphWithNestedTraps_2();
    MutableExceptionalStmtGraph exceptionalStmtGraph = new MutableExceptionalStmtGraph(graph);

    Map<Stmt, List<Stmt>> expectedExceptionalPreds = expectedPredsForGraphWithNestedTraps_2();
    Map<Stmt, List<Stmt>> expectedExceptionalSuccs = expectedSuccsForGraphWithNestedTraps_2();
    Map<Stmt, List<Trap>> expectedExceptionalDests = expectedDestsForGraphWithNestedTraps_2();

    for (Stmt stmt : expectedExceptionalPreds.keySet()) {
      assertStmtsListsEquiv(
          expectedExceptionalPreds.get(stmt), exceptionalStmtGraph.exceptionalPredecessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalSuccs.get(stmt), exceptionalStmtGraph.exceptionalSuccessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalDests.get(stmt), exceptionalStmtGraph.getDestTraps(stmt));
    }
  }

  @Test
  public void graphWithChainedTrapsTest() {
    StmtGraph graph = createGraphWithChainedTraps();
    MutableExceptionalStmtGraph exceptionalStmtGraph = new MutableExceptionalStmtGraph(graph);

    Map<Stmt, List<Stmt>> expectedExceptionalPreds = expectedPredsForGraphWithChainedTraps();
    Map<Stmt, List<Stmt>> expectedExceptionalSuccs = expectedSuccsForGraphWithChainedTraps();
    Map<Stmt, List<Trap>> expectedExceptionalDests = expectedDestsForGraphWithChainedTraps();

    for (Stmt stmt : expectedExceptionalPreds.keySet()) {
      assertStmtsListsEquiv(
          expectedExceptionalPreds.get(stmt), exceptionalStmtGraph.exceptionalPredecessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalSuccs.get(stmt), exceptionalStmtGraph.exceptionalSuccessors(stmt));
      assertStmtsListsEquiv(
          expectedExceptionalDests.get(stmt), exceptionalStmtGraph.getDestTraps(stmt));
    }
  }

  private StmtGraph createGraphWithNestedTraps_1() {
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.setStartingStmt(startingStmt);
    graph.putEdge(startingStmt, label1Stmt);
    graph.putEdge(label1Stmt, label2Stmt);
    graph.putEdge(label2Stmt, stmtInLabel2);
    graph.putEdge(stmtInLabel2, label3Stmt);
    graph.putEdge(label3Stmt, label4Stmt);

    // handler block for trap1
    graph.putEdge(label6Stmt, stmtInLabel6);
    graph.putEdge(stmtInLabel6, gotoStmt1);
    graph.putEdge(gotoStmt1, ret);
    // handler block for trap2
    graph.putEdge(label7Stmt, label8Stmt);
    graph.putEdge(label8Stmt, gotoStmt2);
    graph.putEdge(gotoStmt2, ret);

    graph.putEdge(label4Stmt, ret);
    List<Trap> traps = ImmutableUtils.immutableList(trap1, trap2);
    graph.setTraps(traps);

    return graph;
  }

  private Map<Stmt, List<Stmt>> expectedPredsForGraphWithNestedTraps_1() {

    Map<Stmt, List<Stmt>> predsMap = new HashMap<>();
    List<Stmt> preds_0 = ImmutableUtils.immutableList(label1Stmt, label3Stmt);
    List<Stmt> preds_1 = ImmutableUtils.immutableList(label2Stmt, stmtInLabel2);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label6Stmt) {
        predsMap.put(stmt, preds_0);
      } else if (stmt == label7Stmt) {
        predsMap.put(stmt, preds_1);
      } else {
        predsMap.put(stmt, Collections.emptyList());
      }
    }
    return predsMap;
  }

  private Map<Stmt, List<Stmt>> expectedSuccsForGraphWithNestedTraps_1() {

    Map<Stmt, List<Stmt>> succsMap = new HashMap<>();
    List<Stmt> succs_0 = ImmutableUtils.immutableList(label6Stmt);
    List<Stmt> succs_1 = ImmutableUtils.immutableList(label7Stmt);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label3Stmt) {
        succsMap.put(stmt, succs_0);
      } else if (stmt == label2Stmt || stmt == stmtInLabel2) {
        succsMap.put(stmt, succs_1);
      } else {
        succsMap.put(stmt, Collections.emptyList());
      }
    }
    return succsMap;
  }

  private Map<Stmt, List<Trap>> expectedDestsForGraphWithNestedTraps_1() {

    Map<Stmt, List<Trap>> destsMap = new HashMap<>();
    List<Trap> dests_0 = ImmutableUtils.immutableList(trap1);
    List<Trap> dests_1 = ImmutableUtils.immutableList(trap2);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label3Stmt) {
        destsMap.put(stmt, dests_0);
      } else if (stmt == label2Stmt || stmt == stmtInLabel2) {
        destsMap.put(stmt, dests_1);
      } else {
        destsMap.put(stmt, Collections.emptyList());
      }
    }
    return destsMap;
  }

  private StmtGraph createGraphWithNestedTraps_2() {
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.setStartingStmt(startingStmt);
    graph.putEdge(startingStmt, label1Stmt);
    graph.putEdge(label1Stmt, label2Stmt);
    graph.putEdge(label2Stmt, stmtInLabel2);
    graph.putEdge(stmtInLabel2, label3Stmt);
    graph.putEdge(label3Stmt, label4Stmt);

    // handler block for trap1
    graph.putEdge(label6Stmt, stmtInLabel6);
    graph.putEdge(stmtInLabel6, gotoStmt1);
    graph.putEdge(gotoStmt1, ret);
    // handler block for trap3
    graph.putEdge(label7Stmt, label8Stmt);
    graph.putEdge(label8Stmt, gotoStmt2);
    graph.putEdge(gotoStmt2, ret);

    graph.putEdge(label4Stmt, ret);
    List<Trap> traps = ImmutableUtils.immutableList(trap1, trap3);
    graph.setTraps(traps);

    return graph;
  }

  private Map<Stmt, List<Stmt>> expectedPredsForGraphWithNestedTraps_2() {

    Map<Stmt, List<Stmt>> predsMap = new HashMap<>();
    List<Stmt> preds_0 =
        ImmutableUtils.immutableList(label1Stmt, label2Stmt, stmtInLabel2, label3Stmt);
    List<Stmt> preds_1 = ImmutableUtils.immutableList(label2Stmt, stmtInLabel2);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label6Stmt) {
        predsMap.put(stmt, preds_0);
      } else if (stmt == label7Stmt) {
        predsMap.put(stmt, preds_1);
      } else {
        predsMap.put(stmt, Collections.emptyList());
      }
    }
    return predsMap;
  }

  private Map<Stmt, List<Stmt>> expectedSuccsForGraphWithNestedTraps_2() {

    Map<Stmt, List<Stmt>> succsMap = new HashMap<>();
    List<Stmt> succs_0 = ImmutableUtils.immutableList(label6Stmt);
    List<Stmt> succs_1 = ImmutableUtils.immutableList(label6Stmt, label7Stmt);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label3Stmt) {
        succsMap.put(stmt, succs_0);
      } else if (stmt == label2Stmt || stmt == stmtInLabel2) {
        succsMap.put(stmt, succs_1);
      } else {
        succsMap.put(stmt, Collections.emptyList());
      }
    }
    return succsMap;
  }

  private Map<Stmt, List<Trap>> expectedDestsForGraphWithNestedTraps_2() {

    Map<Stmt, List<Trap>> destsMap = new HashMap<>();
    List<Trap> dests_0 = ImmutableUtils.immutableList(trap1);
    List<Trap> dests_1 = ImmutableUtils.immutableList(trap1, trap3);

    StmtGraph graph = createGraphWithNestedTraps_1();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label3Stmt) {
        destsMap.put(stmt, dests_0);
      } else if (stmt == label2Stmt || stmt == stmtInLabel2) {
        destsMap.put(stmt, dests_1);
      } else {
        destsMap.put(stmt, Collections.emptyList());
      }
    }
    return destsMap;
  }

  private StmtGraph createGraphWithChainedTraps() {
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.setStartingStmt(startingStmt);
    graph.putEdge(startingStmt, label1Stmt);
    graph.putEdge(label1Stmt, label2Stmt);
    graph.putEdge(label2Stmt, label3Stmt);

    // handler block for trap4
    graph.putEdge(label6Stmt, stmtInLabel6);
    graph.putEdge(stmtInLabel6, ret);
    // handler block for trap5
    graph.putEdge(label7Stmt, label8Stmt);
    graph.putEdge(label8Stmt, throwStmt);
    // handler block for trap6
    graph.putEdge(label9Stmt, gotoStmt1);
    graph.putEdge(gotoStmt1, throwStmt);

    graph.putEdge(label3Stmt, ret);
    List<Trap> traps = ImmutableUtils.immutableList(trap4, trap5, trap6);
    graph.setTraps(traps);

    return graph;
  }

  private Map<Stmt, List<Stmt>> expectedPredsForGraphWithChainedTraps() {

    Map<Stmt, List<Stmt>> predsMap = new HashMap<>();
    List<Stmt> preds_0 = ImmutableUtils.immutableList(label1Stmt, label2Stmt);
    List<Stmt> preds_1 = ImmutableUtils.immutableList(label1Stmt, label2Stmt, label6Stmt);
    List<Stmt> preds_2 =
        ImmutableUtils.immutableList(label1Stmt, label2Stmt, label6Stmt, label7Stmt);

    StmtGraph graph = createGraphWithChainedTraps();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label6Stmt) {
        predsMap.put(stmt, preds_0);
      } else if (stmt == label7Stmt) {
        predsMap.put(stmt, preds_1);
      } else if (stmt == label9Stmt) {
        predsMap.put(stmt, preds_2);
      } else {
        predsMap.put(stmt, Collections.emptyList());
      }
    }
    return predsMap;
  }

  private Map<Stmt, List<Stmt>> expectedSuccsForGraphWithChainedTraps() {

    Map<Stmt, List<Stmt>> succsMap = new HashMap<>();
    List<Stmt> succs_0 = ImmutableUtils.immutableList(label6Stmt, label7Stmt, label9Stmt);
    List<Stmt> succs_1 = ImmutableUtils.immutableList(label7Stmt, label9Stmt);
    List<Stmt> succs_2 = ImmutableUtils.immutableList(label9Stmt);

    StmtGraph graph = createGraphWithChainedTraps();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label2Stmt) {
        succsMap.put(stmt, succs_0);
      } else if (stmt == label6Stmt) {
        succsMap.put(stmt, succs_1);
      } else if (stmt == label7Stmt) {
        succsMap.put(stmt, succs_2);
      } else {
        succsMap.put(stmt, Collections.emptyList());
      }
    }
    return succsMap;
  }

  private Map<Stmt, List<Trap>> expectedDestsForGraphWithChainedTraps() {

    Map<Stmt, List<Trap>> destsMap = new HashMap<>();
    List<Trap> dests_0 = ImmutableUtils.immutableList(trap4);
    List<Trap> dests_1 = ImmutableUtils.immutableList(trap5);
    List<Trap> dests_2 = ImmutableUtils.immutableList(trap6);

    StmtGraph graph = createGraphWithChainedTraps();
    Iterator<Stmt> it = graph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      if (stmt == label1Stmt || stmt == label2Stmt) {
        destsMap.put(stmt, dests_0);
      } else if (stmt == label6Stmt) {
        destsMap.put(stmt, dests_1);
      } else if (stmt == label7Stmt) {
        destsMap.put(stmt, dests_2);
      } else {
        destsMap.put(stmt, Collections.emptyList());
      }
    }
    return destsMap;
  }

  // assert whether two stmt lists are equal
  public static void assertStmtsListsEquiv(List expected, List actual) {

    assertNotNull(expected);
    assertNotNull(actual);
    if (expected.size() != actual.size()) {
      System.out.println("Expected size is not equal to actual size: ");
      System.out.println("expected size of list: " + expected.size());
      System.out.println("actual size of list: " + actual.size());
    }
    assertEquals(expected.size(), actual.size());
    boolean condition = true;
    for (Object o : actual) {
      if (!expected.contains(o)) {
        condition = false;
        break;
      }
    }
    if (!condition) {
      System.out.println("expected:");
      System.out.println(expected);
      System.out.println("actual:");
      System.out.println(actual);
    }
    assertTrue(condition);
  }
}
