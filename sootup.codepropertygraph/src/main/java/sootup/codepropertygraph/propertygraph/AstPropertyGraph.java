package sootup.codepropertygraph.propertygraph;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.util.PropertyGraphToDotConverter;

public final class AstPropertyGraph implements PropertyGraph {
  private final String name;
  private final List<PropertyGraphNode> nodes;
  private final List<PropertyGraphEdge> edges;

  private AstPropertyGraph(
      String name, List<PropertyGraphNode> nodes, List<PropertyGraphEdge> edges) {
    this.name = name;
    this.nodes = Collections.unmodifiableList(nodes);
    this.edges = Collections.unmodifiableList(edges);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<PropertyGraphNode> getNodes() {
    return nodes;
  }

  @Override
  public List<PropertyGraphEdge> getEdges() {
    return edges;
  }

  @Override
  public String toDotGraph() {
    return PropertyGraphToDotConverter.convert(this);
  }

  public static class Builder implements PropertyGraph.Builder {
    private final List<PropertyGraphNode> nodes = new ArrayList<>();
    private final List<PropertyGraphEdge> edges = new ArrayList<>();
    private String name;

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    @Override
    public Builder addNode(PropertyGraphNode node) {
      if (!nodes.contains(node)) {
        nodes.add(node);
      }
      return this;
    }

    @Override
    public Builder addEdge(PropertyGraphEdge edge) {
      addNode(edge.getSource());
      addNode(edge.getDestination());
      if (!edges.contains(edge)) {
        edges.add(edge);
      }
      return this;
    }

    @Override
    public PropertyGraph build() {
      return new AstPropertyGraph(name, nodes, edges);
    }
  }
}
