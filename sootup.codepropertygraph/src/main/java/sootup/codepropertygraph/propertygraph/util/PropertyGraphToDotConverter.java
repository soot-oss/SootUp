package sootup.codepropertygraph.propertygraph.util;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
* %%
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation, either version 2.1 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Lesser Public License for more details.
*
* You should have received a copy of the GNU General Lesser Public
* License along with this program.  If not, see
* <http://www.gnu.org/licenses/lgpl-2.1.html>.
* #L%
*/

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.codepropertygraph.propertygraph.nodes.*;

/** Utility class to convert a property graph to DOT format for visualization. */
public class PropertyGraphToDotConverter {

  /**
   * Converts the given property graph to a DOT format string.
   *
   * @param graph the property graph
   * @return the DOT format string
   */
  public static String convert(PropertyGraph graph) {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("digraph %s {\n", graph.getName()));
    builder.append("\trankdir=TB;\n");
    builder.append("\tnode [style=filled, shape=record];\n");
    builder.append("\tedge [style=filled]\n");

    Map<PropertyGraphNode, String> nodeIds = new LinkedHashMap<>();
    AtomicInteger nodeIdCounter = new AtomicInteger(1);

    // Sort nodes by a consistent property
    List<PropertyGraphNode> sortedNodes =
        graph.getNodes().stream()
            .sorted(Comparator.comparing(PropertyGraphNode::toString))
            .collect(Collectors.toList());

    for (PropertyGraphNode node : sortedNodes) {
      String nodeId =
          nodeIds.computeIfAbsent(node, k -> String.valueOf(nodeIdCounter.getAndIncrement()));

      String label = getNodeLabel(node);
      String color = getNodeColor(node);
      builder.append(
          String.format("\t\"%s\" [label=\"%s\", fillcolor=\"%s\"];\n", nodeId, label, color));
    }

    // Sort edges by the IDs of the source and destination nodes
    List<PropertyGraphEdge> sortedEdges =
        graph.getEdges().stream()
            .sorted(
                Comparator.comparing((PropertyGraphEdge edge) -> nodeIds.get(edge.getSource()))
                    .thenComparing(edge -> nodeIds.get(edge.getDestination()))
                    .thenComparing(PropertyGraphEdge::getLabel))
            .collect(Collectors.toList());

    for (PropertyGraphEdge edge : sortedEdges) {
      String sourceId = nodeIds.get(edge.getSource());
      String destinationId = nodeIds.get(edge.getDestination());

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

  /**
   * Escapes special characters in the DOT label.
   *
   * @param label the label
   * @return the escaped label
   */
  private static String escapeDot(String label) {
    return label
        .replace("\"", "\\\"")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("{", "\\{")
        .replace("}", "\\}");
  }

  /**
   * Gets the label for the given node.
   *
   * @param node the node
   * @return the label
   */
  private static String getNodeLabel(PropertyGraphNode node) {
    return escapeDot(node.toString());
  }

  /**
   * Gets the color for the given node.
   *
   * @param node the node
   * @return the color
   */
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

  /**
   * Gets the color for the given edge.
   *
   * @param edge the edge
   * @return the color
   */
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
