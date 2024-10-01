package sootup.analysis.interprocedural.icfg;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;

/**
 * Same as {@link JimpleBasedInterproceduralCFG} but based on inverted Stmt graphs. This should be
 * used for backward analyses.
 */
public class BackwardsInterproceduralCFG implements BiDiInterproceduralCFG<Stmt, SootMethod> {

  protected final BiDiInterproceduralCFG<Stmt, SootMethod> delegate;

  public BackwardsInterproceduralCFG(BiDiInterproceduralCFG<Stmt, SootMethod> fwICFG) {
    delegate = fwICFG;
  }

  // swapped
  @Override
  public List<Stmt> getSuccsOf(Stmt n) {
    return delegate.getPredsOf(n);
  }

  // swapped
  @Override
  public Collection<Stmt> getStartPointsOf(SootMethod m) {
    return delegate.getEndPointsOf(m);
  }

  // swapped
  @Override
  public List<Stmt> getReturnSitesOfCallAt(Stmt n) {
    return delegate.getPredsOfCallAt(n);
  }

  // swapped
  @Override
  public boolean isExitStmt(Stmt stmt) {
    return delegate.isStartPoint(stmt);
  }

  // swapped
  @Override
  public boolean isStartPoint(Stmt stmt) {
    return delegate.isExitStmt(stmt);
  }

  // swapped
  @Override
  public Set<Stmt> allNonCallStartNodes() {
    return delegate.allNonCallEndNodes();
  }

  // swapped
  @Override
  public List<Stmt> getPredsOf(Stmt u) {
    return delegate.getSuccsOf(u);
  }

  // swapped
  @Override
  public Collection<Stmt> getEndPointsOf(SootMethod m) {
    return delegate.getStartPointsOf(m);
  }

  // swapped
  @Override
  public List<Stmt> getPredsOfCallAt(Stmt u) {
    return delegate.getSuccsOf(u);
  }

  // swapped
  @Override
  public Set<Stmt> allNonCallEndNodes() {
    return delegate.allNonCallStartNodes();
  }

  // same
  @Override
  public SootMethod getMethodOf(Stmt n) {
    return delegate.getMethodOf(n);
  }

  // same
  @Override
  public Collection<SootMethod> getCalleesOfCallAt(Stmt n) {
    return delegate.getCalleesOfCallAt(n);
  }

  // same
  @Override
  public Collection<Stmt> getCallersOf(SootMethod m) {
    return delegate.getCallersOf(m);
  }

  // same
  @Override
  public Set<Stmt> getCallsFromWithin(SootMethod m) {
    return delegate.getCallsFromWithin(m);
  }

  // same
  @Override
  public boolean isCallStmt(Stmt stmt) {
    return delegate.isCallStmt(stmt);
  }

  // same
  @Override
  public StmtGraph<?> getOrCreateStmtGraph(SootMethod m) {
    return delegate.getOrCreateStmtGraph(m);
  }

  // same
  @Override
  public List<Value> getParameterRefs(SootMethod m) {
    return delegate.getParameterRefs(m);
  }

  @Override
  public boolean isFallThroughSuccessor(Stmt stmt, Stmt succ) {
    throw new UnsupportedOperationException("not implemented because semantics unclear");
  }

  @Override
  public boolean isBranchTarget(Stmt stmt, Stmt succ) {
    throw new UnsupportedOperationException("not implemented because semantics unclear");
  }

  // swapped
  @Override
  public boolean isReturnSite(Stmt n) {
    for (Stmt pred : getSuccsOf(n)) {
      if (isCallStmt(pred)) {
        return true;
      }
    }
    return false;
  }

  // same
  @Override
  public boolean isReachable(Stmt u) {
    return delegate.isReachable(u);
  }
}
