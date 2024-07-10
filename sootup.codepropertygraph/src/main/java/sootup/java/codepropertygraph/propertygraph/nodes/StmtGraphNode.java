package sootup.java.codepropertygraph.propertygraph.nodes;

import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphNode;

public class StmtGraphNode extends PropertyGraphNode {
  private final Stmt stmt;

  public StmtGraphNode(Stmt stmt) {
    this.stmt = stmt;
  }

  public Stmt getStmt() {
    return stmt;
  }

  @Override
  public String toString() {
    return stmt.toString();
  }
}
