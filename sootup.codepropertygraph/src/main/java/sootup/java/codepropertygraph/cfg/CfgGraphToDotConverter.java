package sootup.java.codepropertygraph.cfg;

class CfgGraphToDotConverter {
  public static String convertToDot(CfgGraph graph) {
    StringBuilder builder = new StringBuilder();
    builder.append("digraph CFG {\n");
    builder.append("\trankdir=TB;\n");
    builder.append("\tnode [shape=record, style=filled];\n");

    for (CfgNode node : graph.getNodes()) {
      String type = escapeDot(node.getType().toString());
      String name = escapeDot(node.getName());
      String label = String.format("\"{<f0> %s | <f1> %s}\"", type, name);
      String color = getTypeBasedColor(node.getType());
      builder.append(
          String.format("\t\"%s\" [label=%s, fillcolor=\"%s\"];\n", node.hashCode(), label, color));
    }

    for (CfgEdge edge : graph.getEdges()) {
      String sourceId = String.valueOf(edge.getSource().hashCode());
      String destinationId = String.valueOf(edge.getDestination().hashCode());
      builder.append(String.format("\t\"%s\" -> \"%s\";\n", sourceId, destinationId));
    }

    builder.append("}\n");
    return builder.toString();
  }

  private static String escapeDot(String label) {
    return label.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static String getTypeBasedColor(CfgNodeType type) {
    if (type == CfgNodeType.STMT) return "lightblue";
    return "white";
  }
}
