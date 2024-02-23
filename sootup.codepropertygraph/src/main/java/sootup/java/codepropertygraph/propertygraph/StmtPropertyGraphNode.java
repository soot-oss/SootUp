package sootup.java.codepropertygraph.propertygraph;

import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;

public class StmtPropertyGraphNode extends PropertyGraphNode {
  private final StmtPositionInfo positionInfo;
  private final Stmt stmt;

  public StmtPropertyGraphNode(
      String name, NodeType type, StmtPositionInfo positionInfo, Stmt stmt) {
    super(name, type);
    this.positionInfo = positionInfo;
    this.stmt = stmt;
  }

  public StmtPositionInfo getPositionInfo() {
    return positionInfo;
  }

  public Stmt getStmt() {
    return stmt;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) return false;
    if (getClass() != o.getClass()) return false;
    StmtPropertyGraphNode that = (StmtPropertyGraphNode) o;
    return positionInfo.getStmtPosition().compareTo(that.positionInfo.getStmtPosition()) == 0;
  }
}
