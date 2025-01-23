package sootup.core.graph;

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JAddExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JLeExpr;
import sootup.core.jimple.common.expr.JLtExpr;
import sootup.core.jimple.common.stmt.*;
import sootup.core.types.PrimitiveType;

public class TestGraphGenerator {

  StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();

  public MutableBlockStmtGraph createStmtGraph() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    Local l2 = new Local("l2", PrimitiveType.IntType.getInstance());
    Local l3 = new Local("l3", PrimitiveType.IntType.getInstance());

    JAssignStmt assign01 = new JAssignStmt(l1, IntConstant.getInstance(1), noPosInfo);
    JAssignStmt assign02 = new JAssignStmt(l2, IntConstant.getInstance(2), noPosInfo);
    JAssignStmt assign03 = new JAssignStmt(l2, IntConstant.getInstance(0), noPosInfo);
    JAssignStmt assign41 = new JAssignStmt(l2, l3, noPosInfo);
    JAssignStmt assign42 =
        new JAssignStmt(l3, new JAddExpr(l3, IntConstant.getInstance(2)), noPosInfo);
    JAssignStmt assign51 = new JAssignStmt(l2, l1, noPosInfo);
    JAssignStmt assign52 =
        new JAssignStmt(l3, new JAddExpr(l3, IntConstant.getInstance(1)), noPosInfo);

    BranchingStmt if1 = new JIfStmt(new JLeExpr(l3, IntConstant.getInstance(100)), noPosInfo);
    BranchingStmt if3 = new JIfStmt(new JLeExpr(l2, IntConstant.getInstance(20)), noPosInfo);

    JReturnStmt return2 = new JReturnStmt(l2, noPosInfo);
    JGotoStmt goto4 = new JGotoStmt(noPosInfo);
    JGotoStmt goto5 = new JGotoStmt(noPosInfo);
    JGotoStmt goto6 = new JGotoStmt(noPosInfo);

    // block 0
    graph.setStartingStmt(assign01);
    graph.putEdge(assign01, assign02);
    graph.putEdge(assign02, assign03);
    graph.putEdge(assign03, if1);

    // block 1
    graph.putEdge(if1, JIfStmt.FALSE_BRANCH_IDX, return2);
    graph.putEdge(if1, JIfStmt.TRUE_BRANCH_IDX, if3);

    // block 2
    graph.putEdge(if3, JIfStmt.FALSE_BRANCH_IDX, assign41);
    graph.putEdge(if3, JIfStmt.TRUE_BRANCH_IDX, assign51);

    // block 3
    graph.putEdge(assign41, assign42);
    graph.putEdge(assign42, goto4);
    graph.putEdge(goto4, JGotoStmt.BRANCH_IDX, goto6);

    // block 4
    graph.putEdge(assign51, assign52);
    graph.putEdge(assign52, goto5);
    graph.putEdge(goto5, JGotoStmt.BRANCH_IDX, goto6);

    // block 5
    graph.putEdge(goto6, JGotoStmt.BRANCH_IDX, if1);
    return graph;
  }

  // a control flow graph without returnStmt, because simulate the situation in the paper
  // if it doesn't pass the validation, please add a return stmt after ifl2l50
  public MutableBlockStmtGraph createStmtGraph2() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    Local l2 = new Local("l2", PrimitiveType.IntType.getInstance());
    Local l3 = new Local("l3", PrimitiveType.IntType.getInstance());

    JAssignStmt assignl1e1 = new JAssignStmt(l1, IntConstant.getInstance(1), noPosInfo);
    JAssignStmt assignl2e1 = new JAssignStmt(l2, IntConstant.getInstance(1), noPosInfo);
    JAssignStmt assignl3e0 = new JAssignStmt(l3, IntConstant.getInstance(0), noPosInfo);

    JAssignStmt assignl3e1 = new JAssignStmt(l3, IntConstant.getInstance(1), noPosInfo);

    JAssignStmt assignl1aa =
        new JAssignStmt(l1, new JAddExpr(l1, IntConstant.getInstance(1)), noPosInfo);
    JAssignStmt assignl2aa =
        new JAssignStmt(l2, new JAddExpr(l2, IntConstant.getInstance(1)), noPosInfo);
    JAssignStmt assignl3el2al1 = new JAssignStmt(l3, new JAddExpr(l1, l2), noPosInfo);

    BranchingStmt ifl1e1 = new JIfStmt(new JEqExpr(l1, IntConstant.getInstance(1)), noPosInfo);
    BranchingStmt ifl3e1 = new JIfStmt(new JEqExpr(l3, IntConstant.getInstance(1)), noPosInfo);
    BranchingStmt ifl1l50 = new JIfStmt(new JLtExpr(l1, IntConstant.getInstance(50)), noPosInfo);
    BranchingStmt ifl2l50 = new JIfStmt(new JLtExpr(l2, IntConstant.getInstance(50)), noPosInfo);

    // JReturnStmt returnl3 = new JReturnStmt(l3, noPosInfo);

    // block 0
    graph.setStartingStmt(assignl1e1);
    graph.putEdge(assignl1e1, assignl2e1);
    graph.putEdge(assignl2e1, assignl3e0);
    graph.putEdge(assignl3e0, ifl1e1);
    graph.putEdge(ifl1e1, JIfStmt.TRUE_BRANCH_IDX, assignl3e1);
    graph.putEdge(ifl1e1, JIfStmt.FALSE_BRANCH_IDX, ifl3e1);

    // block 1
    graph.putEdge(assignl3e1, assignl1aa);

    // block 2
    graph.putEdge(ifl3e1, JIfStmt.TRUE_BRANCH_IDX, assignl3el2al1);
    graph.putEdge(ifl3e1, JIfStmt.FALSE_BRANCH_IDX, assignl2aa);

    // block 3
    graph.putEdge(assignl1aa, assignl3el2al1);

    // block 4
    graph.putEdge(assignl2aa, ifl2l50);
    graph.putEdge(ifl2l50, JIfStmt.FALSE_BRANCH_IDX, assignl3el2al1);
    // graph.putEdge(ifl2l50, JIfStmt.TRUE_BRANCH_IDX, returnl3);

    // block 5
    graph.putEdge(assignl3el2al1, ifl1l50);
    graph.putEdge(ifl1l50, JIfStmt.FALSE_BRANCH_IDX, assignl1aa);
    graph.putEdge(ifl1l50, JIfStmt.TRUE_BRANCH_IDX, assignl2aa);

    return graph;
  }

  // a graph with two end blocks
  public MutableBlockStmtGraph createStmtGraph3() {
    MutableBlockStmtGraph graph = new MutableBlockStmtGraph();

    Local l1 = new Local("l1", PrimitiveType.IntType.getInstance());
    Local l2 = new Local("l2", PrimitiveType.IntType.getInstance());
    Local l3 = new Local("l3", PrimitiveType.IntType.getInstance());

    JAssignStmt assignl1e1 = new JAssignStmt(l1, IntConstant.getInstance(1), noPosInfo);
    JAssignStmt assignl2e1 = new JAssignStmt(l2, IntConstant.getInstance(2), noPosInfo);
    JAssignStmt assignl3e0 = new JAssignStmt(l3, IntConstant.getInstance(0), noPosInfo);

    JAssignStmt assignl3e1 = new JAssignStmt(l3, IntConstant.getInstance(1), noPosInfo);

    BranchingStmt ifl2e100 = new JIfStmt(new JLtExpr(l3, IntConstant.getInstance(100)), noPosInfo);

    JReturnStmt returnl1 = new JReturnStmt(l1, noPosInfo);
    JReturnStmt returnl2 = new JReturnStmt(l2, noPosInfo);

    graph.setStartingStmt(assignl1e1);
    graph.putEdge(assignl1e1, assignl2e1);
    graph.putEdge(assignl2e1, assignl3e0);
    graph.putEdge(assignl3e0, ifl2e100);
    graph.putEdge(ifl2e100, JIfStmt.TRUE_BRANCH_IDX, returnl1);
    graph.putEdge(ifl2e100, JIfStmt.FALSE_BRANCH_IDX, returnl2);

    return graph;
  }
}
