package sootup.java.codepropertygraph.propertygraph;

class PropertyGraphToDotConverter {
  public static String convert(PropertyGraph graph, String graphName) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("digraph %s {\n", graphName));
    builder.append("\trankdir=TB;\n");
    builder.append("\tnode [style=filled, shape=record];\n");
    builder.append("\tedge [style=filled]");

    for (PropertyGraphNode node : graph.getNodes()) {
      String label = getNodeLabel(node);
      String color = getNodeColor(node.getType());
      builder.append(
          String.format("\t\"%s\" [label=%s, fillcolor=\"%s\"];\n", node.hashCode(), label, color));
    }

    for (PropertyGraphEdge edge : graph.getEdges()) {
      String sourceId = String.valueOf(edge.getSource().hashCode());
      String destinationId = String.valueOf(edge.getDestination().hashCode());

      String label = edge.getLabel();
      String color = getEdgeColor(label);
      builder.append(
          String.format(
              "\t\"%s\" -> \"%s\"[label=\"%s\", color=\"%s\", fontcolor=\"%s\"];\n",
              sourceId, destinationId, label, color, color));
    }

    builder.append("}\n");
    return builder.toString();
  }

  private static String getNodeLabel(PropertyGraphNode node) {
    String name = escapeDot(node.getName());
    String typeStr = escapeDot(node.getType().toString());

    if (node.getType() == NodeType.AGGREGATE) return name;

    return String.format("\"{<f0> %s | <f1> %s}\"", typeStr, name);
  }

  private static String escapeDot(String label) {
    return label.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static String getNodeColor(NodeType type) {
    switch (type) {
      case STMT:
      case RETURN_TYPE:
        return "lightblue";
      case MODIFIER:
        return "darkseagreen2";
      case PARAMETER_TYPE:
        return "lightgray";
      case CMP:
      case LEFTOP:
      case RIGHTOP:
      case OP1:
      case COND:
      case OP2:
      case POS:
      case OP:
        return "beige";
      default:
        return "white";
    }
  }

  private static String getEdgeColor(String edgeLabel) {
    if (edgeLabel.startsWith("AST")) return "darkseagreen4";
    if (edgeLabel.startsWith("CFG")) return "darkred";
    if (edgeLabel.startsWith("CDG") || edgeLabel.startsWith("DDG")) return "dodgerblue4";
    return "black";
  }
}
