package sootup.spark;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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

import java.io.StringWriter;
import java.util.function.Function;
import org.graph4j.Edge;
import org.graph4j.Graph;

public class PAGVisualizer {

  public static <T> String visualizeMethodPAG(Graph graph, Function<T, String> vertexToString) {
    StringWriter writer = new StringWriter();
    writer.write("digraph G {\n");

    // Vertices
    for (int v = 0; v < graph.numVertices(); v++) {
      Object label = graph.getVertexLabel(v);
      writer.write("  " + v + " [label=\"" + vertexToString.apply((T) label) + "\"];\n");
    }

    // Edges
    for (Edge edge : graph.edges()) {
      int source = edge.source();
      int target = edge.target();
      writer.write("  " + source + " -> " + target + ";\n");
    }

    writer.write("}");
    return writer.toString();
  }
}
