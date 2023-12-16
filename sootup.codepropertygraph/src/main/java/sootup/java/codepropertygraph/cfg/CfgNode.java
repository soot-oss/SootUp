package sootup.java.codepropertygraph.cfg;

import java.util.Objects;
import sootup.core.jimple.basic.StmtPositionInfo;

public class CfgNode {
  private final String name;
  private final CfgNodeType type;
  private final StmtPositionInfo positionInfo;

  public CfgNode(String name, CfgNodeType type, StmtPositionInfo positionInfo) {
    this.name = name;
    this.type = type;
    this.positionInfo = positionInfo;
  }

  public String getName() {
    return name;
  }

  public CfgNodeType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CfgNode cfgNode = (CfgNode) o;
    return Objects.equals(name, cfgNode.name)
        && type == cfgNode.type
        && Objects.equals(positionInfo, cfgNode.positionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, positionInfo);
  }
}
