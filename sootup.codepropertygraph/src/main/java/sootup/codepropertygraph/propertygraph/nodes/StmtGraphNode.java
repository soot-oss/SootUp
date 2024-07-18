package sootup.codepropertygraph.propertygraph.nodes;

import java.util.Objects;
import sootup.core.jimple.common.stmt.Stmt;

public class StmtGraphNode extends PropertyGraphNode {
  private final Stmt stmt;

  public StmtGraphNode(Stmt stmt) {
    this.stmt = stmt;
  }

  public Stmt getStmt() {
    return stmt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StmtGraphNode that = (StmtGraphNode) o;
    return stmt.equivTo(that.getStmt())
        && Objects.equals(this.stmt.getPositionInfo(), that.getStmt().getPositionInfo());
  }

  @Override
  public int hashCode() {
    return Objects.hash(stmt.toString(), stmt.getPositionInfo());
  }

  @Override
  public String toString() {
    return stmt.toString();
  }
}
