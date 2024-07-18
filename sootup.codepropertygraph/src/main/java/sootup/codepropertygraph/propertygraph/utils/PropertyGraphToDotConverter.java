package sootup.codepropertygraph.propertygraph.utils;

import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;

public class PropertyGraphToDotConverter {
  public static String convert(PropertyGraph graph) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("digraph %s {\n", graph.getName()));
    builder.append("\trankdir=TB;\n");
    builder.append("\tnode [style=filled, shape=record];\n");
    builder.append("\tedge [style=filled]\n");

    for (PropertyGraphNode node : graph.getNodes()) {
      String label = getNodeLabel(node);
      String color = getNodeColor(node);
      builder.append(
          String.format(
              "\t\"%s\" [label=\"%s\", fillcolor=\"%s\"];\n", node.hashCode(), label, color));
    }

    for (PropertyGraphEdge edge : graph.getEdges()) {
      String sourceId = String.valueOf(edge.getSource().hashCode());
      String destinationId = String.valueOf(edge.getDestination().hashCode());

      String label = escapeDot(edge.getLabel());
      String color = getEdgeColor(edge);
      builder.append(
          String.format(
              "\t\"%s\" -> \"%s\"[label=\"%s\", color=\"%s\", fontcolor=\"%s\"];\n",
              sourceId, destinationId, label, color, color));
    }

    builder.append("}\n");
    return builder.toString();
  }

  private static String escapeDot(String label) {
    return label
        .replace("\"", "\\\"")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("{", "\\{")
        .replace("}", "\\}");
  }

  private static String getNodeLabel(PropertyGraphNode node) {
    return escapeDot(node.toString());
  }

  private static String getNodeColor(PropertyGraphNode node) {
    if (node instanceof StmtGraphNode) {
      return "lightblue";
    } else if (node instanceof TypeGraphNode || node instanceof ModifierGraphNode) {
      return "lightgray";
    } else if (node instanceof AggregateGraphNode) {
      return "darkseagreen2";
    }
    return "white";
  }

  private static String getEdgeColor(PropertyGraphEdge edge) {
    if (edge instanceof AbstAstEdge) {
      return "darkseagreen4";
    } else if (edge instanceof AbstCdgEdge) {
      return "dodgerblue4";
    } else if (edge instanceof AbstDdgEdge) {
      return "firebrick";
    }

    return "black";
  }
}
