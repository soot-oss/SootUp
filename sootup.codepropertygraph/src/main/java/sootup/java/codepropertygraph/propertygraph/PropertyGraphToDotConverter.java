package sootup.java.codepropertygraph.propertygraph;

import sootup.java.codepropertygraph.propertygraph.nodes.StmtGraphNode;

class PropertyGraphToDotConverter {
  public static String convert(PropertyGraph graph, String graphName) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("digraph %s {\n", graphName));
    builder.append("\trankdir=TB;\n");
    builder.append("\tnode [style=filled, shape=record];\n");
    builder.append("\tedge [style=filled]");

    for (PropertyGraphNode node : graph.getNodes()) {
      String label = getNodeLabel(node);
      String color = getNodeColor(node);
      builder.append(
          String.format("\t\"%s\" [label=%s, fillcolor=\"%s\"];\n", node.hashCode(), label, color));
    }

    for (PropertyGraphEdge edge : graph.getEdges()) {
      String sourceId = String.valueOf(edge.getSource().hashCode());
      String destinationId = String.valueOf(edge.getDestination().hashCode());

      EdgeType edgeType = edge.getType();
      String label = edgeType.toString();
      String color = getEdgeColor(edgeType);
      builder.append(
          String.format(
              "\t\"%s\" -> \"%s\"[label=\"%s\", color=\"%s\", fontcolor=\"%s\"];\n",
              sourceId, destinationId, label, color, color));
    }

    builder.append("}\n");
    return builder.toString();
  }

  private static String getNodeLabel(PropertyGraphNode node) {
    return escapeDot(node.toString());
  }

  private static String escapeDot(String label) {
    return label.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static String getNodeColor(PropertyGraphNode node) {
    if (node instanceof StmtGraphNode) {
      return "lightblue";
    } else if (node instanceof TypeGraphNode) {
      return "lightgray";
    } else if (node instanceof SimpleGraphNode) {
      return "darkseagreen2";
    }
    return "white";
  }

  private static String getEdgeColor(EdgeType type) {
    switch (type) {
      case AST:
        return "darkseagreen4";
      case CFG:
        return "darkred";
      case CDG:
      case DDG:
        return "dodgerblue4";
      default:
        return "black";
    }
  }
}
