package de.upb.swt.soot.test.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.graph.MutableStmtGraph;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.JTrap;
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
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class ExceptionalStmtGraphTest {

  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();
  JavaJimple javaJimple = JavaJimple.getInstance();

  JavaClassType classType = factory.getClassType("Test");
  IdentityRef identityRef = JavaJimple.newThisRef(classType);
  JavaClassType refType = factory.getClassType("ref");
  ClassType exception = factory.getClassType("Exception");
  IdentityRef caughtExceptionRef = javaJimple.newCaughtExceptionRef();

  // build locals
  Local l0 = JavaJimple.newLocal("l0", refType);
  Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
  Local l2 = JavaJimple.newLocal("l2", PrimitiveType.getInt());
  Local l3 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
  Local l4 = JavaJimple.newLocal("l4", PrimitiveType.getInt());
  Local l5 = JavaJimple.newLocal("l5", PrimitiveType.getInt());
  Local stack6 = JavaJimple.newLocal("stack6", refType);
  Local stack7 = JavaJimple.newLocal("stack7", refType);
  Local l8 = JavaJimple.newLocal("l8", PrimitiveType.getInt());
  Local stack9 = JavaJimple.newLocal("stack9", refType);
  Local l9 = JavaJimple.newLocal("l9", PrimitiveType.getInt());

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
  // l3 = 3
  Stmt label4Stmt = JavaJimple.newAssignStmt(l4, IntConstant.getInstance(4), noStmtPositionInfo);
  // l3 = 3
  Stmt label5Stmt = JavaJimple.newAssignStmt(l5, IntConstant.getInstance(5), noStmtPositionInfo);
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
  Stmt gotoStmt = JavaJimple.newGotoStmt(noStmtPositionInfo);
  Stmt throwStmt = JavaJimple.newThrowStmt(l1, noStmtPositionInfo);
  // stack9 := @caughtexception
  Stmt label9Stmt = JavaJimple.newIdentityStmt(stack9, caughtExceptionRef, noStmtPositionInfo);
  Stmt stmtInLabel9 = JavaJimple.newAssignStmt(l9, IntConstant.getInstance(9), noStmtPositionInfo);

  JTrap trap1 = new JTrap(exception, label1Stmt, label3Stmt, label6Stmt);
  JTrap trap2 = new JTrap(exception, label6Stmt, label8Stmt, label7Stmt);
  JTrap trap3 = new JTrap(exception, label4Stmt, label5Stmt, label9Stmt);
  List<Trap> traps = ImmutableUtils.immutableList(trap1, trap2, trap3);

  @Test
  public void graphTest() {
    StmtGraph graph = createStmtGraph();
    ExceptionalStmtGraph exceptionalStmtGraph = new ExceptionalStmtGraph(graph);

    Iterator<Stmt> it = exceptionalStmtGraph.iterator();
    while (it.hasNext()) {
      Stmt stmt = it.next();
      System.out.println("Stmt: " + stmt);
      System.out.println("Preds: " + exceptionalStmtGraph.predecessors(stmt));
      System.out.println("Succs: " + exceptionalStmtGraph.successors(stmt));
      System.out.println("ExceptionalPreds: " + exceptionalStmtGraph.exceptionalPredecessors(stmt));
      System.out.println("ExceptionalSuccs: " + exceptionalStmtGraph.exceptionalSuccessors(stmt));
      System.out.println("---------------------------------------------------");
    }
  }

  private StmtGraph createStmtGraph() {
    MutableStmtGraph graph = new MutableStmtGraph();
    graph.setStartingStmt(startingStmt);
    graph.putEdge(startingStmt, label1Stmt);
    graph.putEdge(label1Stmt, label2Stmt);
    graph.putEdge(label2Stmt, stmtInLabel2);
    graph.putEdge(stmtInLabel2, label3Stmt);
    graph.putEdge(label3Stmt, label4Stmt);
    graph.putEdge(label4Stmt, label5Stmt);
    graph.putEdge(label5Stmt, ret);

    // trap1
    graph.putEdge(label6Stmt, stmtInLabel6);
    graph.putEdge(stmtInLabel6, throwStmt);
    // trap2
    graph.putEdge(label7Stmt, label8Stmt);
    graph.putEdge(label8Stmt, gotoStmt);
    graph.putEdge(gotoStmt, throwStmt);
    // trap3
    graph.putEdge(label9Stmt, stmtInLabel9);
    graph.putEdge(stmtInLabel9, ret);

    graph.setTraps(traps);
    return graph;
  }
}
