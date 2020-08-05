package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;

// TODO: [ms] implement class - make predecessors/successors explicit for traps/exceptions instead
// of making it work via Trap
public class ExceptionalStmtGraph extends ImmutableStmtGraph {

  ExceptionalStmtGraph(StmtGraph originalGraph) {
    super(originalGraph);
  }

  public static ImmutableStmtGraph copyOf(StmtGraph stmtGraph) {
    if (stmtGraph instanceof ExceptionalStmtGraph) {
      return (ExceptionalStmtGraph) stmtGraph;
    }

    if (stmtGraph.getTraps().isEmpty()) {
      return ImmutableStmtGraph.copyOf(stmtGraph);
    }

    assert (stmtGraph instanceof MutableStmtGraph);

    // adds an edge for *every* stmt between start and end of a block
    // TODO: check whether traps are allowed spreading over more than just one block
    for (Trap trap : stmtGraph.getTraps()) {
      Stmt iteratorStmt = trap.getBeginStmt();
      ((MutableStmtGraph) stmtGraph).putEdge(iteratorStmt, trap.getHandlerStmt());
      while (iteratorStmt.fallsThrough() && iteratorStmt != trap.getEndStmt()) {
        iteratorStmt = stmtGraph.successors(iteratorStmt).get(0);
        ((MutableStmtGraph) stmtGraph).putEdge(iteratorStmt, trap.getHandlerStmt());
      }
    }
    return new ExceptionalStmtGraph(stmtGraph);
  }
}
