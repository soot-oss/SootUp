package sootup.java.codepropertygraph.propertygraph;

import java.util.Objects;
import sootup.core.jimple.basic.StmtPositionInfo;

public class StmtPropertyGraphNode extends PropertyGraphNode {
  private final StmtPositionInfo positionInfo;

  public StmtPropertyGraphNode(String name, NodeType type, StmtPositionInfo positionInfo) {
    super(name, type);
    this.positionInfo = positionInfo;
  }

  public StmtPositionInfo getPositionInfo() {
    return positionInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) return false;
    if (getClass() != o.getClass()) return false;
    StmtPropertyGraphNode that = (StmtPropertyGraphNode) o;
    return Objects.equals(positionInfo, that.positionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), positionInfo);
  }
}
