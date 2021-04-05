package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public final class ExceptionalStmtGraph extends StmtGraph{

    @Nonnull private final MutableExceptionalStmtGraph exceptionalStmtGraph;

    public ExceptionalStmtGraph(@Nonnull MutableExceptionalStmtGraph graph){
        this.exceptionalStmtGraph = graph;
    }
    @Override
    public Stmt getStartingStmt() {
        return exceptionalStmtGraph.getStartingStmt();
    }

    @Nonnull
    @Override
    public Set<Stmt> nodes() {
        return exceptionalStmtGraph.nodes();
    }

    @Override
    public boolean containsNode(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.containsNode(stmt);
    }

    @Nonnull
    @Override
    public List<Stmt> predecessors(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.predecessors(stmt);
    }

    @Nonnull
    public List<Stmt> exceptionalPredecessors(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.exceptionalPredecessors(stmt);
    }

    @Nonnull
    @Override
    public List<Stmt> successors(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.successors(stmt);
    }

    @Nonnull
    public List<Stmt> exceptionalSuccessors(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.exceptionalSuccessors(stmt);
    }

    @Nonnull
    public List<Trap> getDestTraps(@Nonnull Stmt stmt){
        return exceptionalStmtGraph.getDestTraps(stmt);
    }

    @Override
    public int inDegree(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.degree(stmt);
    }

    @Override
    public int outDegree(@Nonnull Stmt stmt) {
        return exceptionalStmtGraph.outDegree(stmt);
    }

    @Override
    public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
        return exceptionalStmtGraph.hasEdgeConnecting(source, target);
    }

    @Nonnull
    @Override
    public List<Trap> getTraps() {
        return exceptionalStmtGraph.getTraps();
    }
}
