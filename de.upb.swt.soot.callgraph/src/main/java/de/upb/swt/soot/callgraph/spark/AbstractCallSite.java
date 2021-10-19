package de.upb.swt.soot.callgraph.spark;

import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;

/**
 * Abstract base class for call sites
 *
 * @author Steven Arzt
 */
public class AbstractCallSite {

  protected Stmt stmt;
  protected SootMethod container;

  public AbstractCallSite(Stmt stmt, SootMethod container) {
    this.stmt = stmt;
    this.container = container;
  }

  public Stmt getStmt() {
    return stmt;
  }

  public SootMethod getContainer() {
    return container;
  }

  @Override
  public String toString() {
    return stmt == null ? "<null>" : stmt.toString();
  }
}