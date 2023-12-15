package sootup.java.codepropertygraph.ddg;

import java.util.Objects;
import sootup.core.jimple.basic.StmtPositionInfo;

public class DdgNode {
  private final String name;
  private final DdgNodeType type;
  private final StmtPositionInfo positionInfo;

  public DdgNode(String name, DdgNodeType type, StmtPositionInfo positionInfo) {
    this.name = name;
    this.type = type;
    this.positionInfo = positionInfo;
  }

  public String getName() {
    return name;
  }

  public DdgNodeType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DdgNode ddgNode = (DdgNode) o;
    return Objects.equals(name, ddgNode.name) && type == ddgNode.type && Objects.equals(positionInfo, ddgNode.positionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, positionInfo);
  }
}
