package sootup.codepropertygraph.propertygraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.MethodGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.codepropertygraph.propertygraph.utils.PropertyGraphToDotConverter;

public final class StmtMethodPropertyGraph implements PropertyGraph {
  private final List<PropertyGraphNode> nodes;
  private final List<PropertyGraphEdge> edges;

  private StmtMethodPropertyGraph(List<PropertyGraphNode> nodes, List<PropertyGraphEdge> edges) {
    this.nodes = Collections.unmodifiableList(nodes);
    this.edges = Collections.unmodifiableList(edges);
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
  public String toDotGraph(String graphName) {
    return PropertyGraphToDotConverter.convert(this, graphName);
  }

  public static class Builder implements PropertyGraph.Builder {
    private final List<PropertyGraphNode> nodes = new ArrayList<>();
    private final List<PropertyGraphEdge> edges = new ArrayList<>();

    @Override
    public Builder addNode(PropertyGraphNode node) {
      if (!(node instanceof StmtGraphNode || node instanceof MethodGraphNode)) {
        throw new IllegalArgumentException("Graph can only contain statement or method nodes");
      }
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
      return new StmtMethodPropertyGraph(nodes, edges);
    }
  }
}
