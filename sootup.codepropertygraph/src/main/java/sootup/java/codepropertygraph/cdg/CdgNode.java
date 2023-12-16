package sootup.java.codepropertygraph.cdg;

import java.util.Objects;
import sootup.core.jimple.basic.StmtPositionInfo;

public class CdgNode {
  private final String name;
  private final CdgNodeType type;
  private final StmtPositionInfo positionInfo;

  public CdgNode(String name, CdgNodeType type, StmtPositionInfo positionInfo) {
    this.name = name;
    this.type = type;
    this.positionInfo = positionInfo;
  }

  public String getName() {
    return name;
  }

  public CdgNodeType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CdgNode cdgNode = (CdgNode) o;
    return Objects.equals(name, cdgNode.name)
        && type == cdgNode.type
        && Objects.equals(positionInfo, cdgNode.positionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, positionInfo);
  }
}
