package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;

public class StmtLocalPair {
  private final Stmt stmt;
  private final Local local;

  public StmtLocalPair(Stmt stmt, Local local) {
    this.stmt = stmt;
    this.local = local;
  }

  public Stmt getStmt() {
    return this.stmt;
  }

  public Local getLocal() {
    return this.local;
  }
}
