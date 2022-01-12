package de.upb.swt.soot.core.graph;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.BranchingStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ClassType;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Structure is like in old soot. use with care! not memory or runtime efficient!.
 *
 * @author Markus Schmidt
 */
public class LegacyStmtGraphList extends MutableStmtGraph {

  private List<Stmt> stmts;
  private List<Trap> traps;
  private HashMap<BranchingStmt, Stmt> branches;

  public LegacyStmtGraphList(
      @Nonnull List<Stmt> stmts,
      @Nonnull HashMap<BranchingStmt, Stmt> branches,
      @Nonnull List<Trap> traps) {
    this.stmts = stmts;
    this.traps = traps;
    this.branches = branches;
  }

  @Nonnull
  @Override
  public StmtGraph unmodifiableStmtGraph() {
    return new ForwardingStmtGraph(this);
  }

  @Override
  public void setStartingStmt(@Nonnull Stmt firstStmt) {
    if (stmts.isEmpty()) {
      stmts.add(firstStmt);
    } else {
      if (stmts.get(0) != firstStmt) {
        stmts.add(0, firstStmt);
      }
    }
  }

  @Override
  public void addNode(@Nonnull Stmt node, @Nonnull List<ClassType> traps) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void addBlock(@Nonnull MutableBasicBlock block) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void replaceNode(@Nonnull Stmt oldStmt, @Nonnull Stmt newStmt) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void removeNode(@Nonnull Stmt node) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void putEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void setEdges(@Nonnull Stmt from, @Nonnull List<Stmt> targets) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void removeEdge(@Nonnull Stmt from, @Nonnull Stmt to) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public void clearExceptionalEdges(@Nonnull Stmt stmt) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public Stmt getStartingStmt() {
    return stmts.size() > 0 ? stmts.get(0) : null;
  }

  @Nonnull
  @Override
  public Collection<Stmt> nodes() {
    return stmts;
  }

  @Nonnull
  @Override
  public List<? extends BasicBlock> getBlocks() {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean containsNode(@Nonnull Stmt node) {
    return stmts.contains(node);
  }

  @Nonnull
  @Override
  public List<Stmt> predecessors(@Nonnull Stmt node) {
    throw new RuntimeException("not implemented");
  }

  @Nonnull
  @Override
  public List<Stmt> successors(@Nonnull Stmt node) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public int inDegree(@Nonnull Stmt node) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public int outDegree(@Nonnull Stmt node) {
    throw new RuntimeException("not implemented");
  }

  @Override
  public boolean hasEdgeConnecting(@Nonnull Stmt source, @Nonnull Stmt target) {
    throw new RuntimeException("not implemented");
  }

  public void setTraps(@Nonnull List<Trap> traps) {
    this.traps = traps;
  }
}
