package org.sootup.java.codepropertygraph.evaluation.graph.util;

import io.shiftleft.codepropertygraph.generated.nodes.StoredNode;
import io.shiftleft.codepropertygraph.generated.nodes.Unknown;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;

public class NodeTypeResolver {
  public boolean isGotoStatement(StoredNode node) {
    return node instanceof Unknown && ((Unknown) node).code().startsWith("goto");
  }

  public boolean isEnterMonitorStatement(StoredNode node) {
    return node instanceof Unknown && ((Unknown) node).code().startsWith("entermonitor");
  }

  public boolean isExitMonitorStatement(StoredNode node) {
    return node instanceof Unknown && ((Unknown) node).code().startsWith("exitmonitor");
  }

  public Type getNodeType(String typeStr) {
    switch (typeStr) {
      case "byte":
        return PrimitiveType.getByte();
      case "int":
        return PrimitiveType.getInt();
      case "long":
        return PrimitiveType.getLong();
      case "float":
        return PrimitiveType.getFloat();
      case "double":
        return PrimitiveType.getDouble();
      case "boolean":
        return PrimitiveType.getBoolean();
      case "short":
        return PrimitiveType.getShort();
      case "char":
        return PrimitiveType.getChar();
      default:
        return JavaIdentifierFactory.getInstance().getType(typeStr);
    }
  }
}
