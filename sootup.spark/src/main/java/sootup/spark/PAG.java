package sootup.spark;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.graph4j.Digraph;
import org.graph4j.GraphBuilder;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.node.VariableNode;

/** Pointer assigment graph */
@Slf4j
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PAG {

  @NonNull Digraph<Node, PAGEdge> delegate = GraphBuilder.empty().buildDigraph();

  public void addEdge(Node source, Node target) {
    if (source instanceof VariableNode) {
      if (target instanceof VariableNode) {
        addEdge(source, target, PAGEdge.assignment());
      } else if (target instanceof InstanceFieldRefNode) {
        addEdge(source, target, PAGEdge.store());
      }
    } else if (source instanceof InstanceFieldRefNode) {
      addEdge(source, target, PAGEdge.load());
    } else if (source instanceof AllocationNode) {
      addEdge(source, target, PAGEdge.allocation());
    } else {
      log.error("Invalid edge type for source: {} -> target: {}", source, target);
    }
  }

  private void addEdge(Node source, Node target, PAGEdge edge) {
    int sIdx = delegate.addLabeledVertex(source);
    int tIdx = delegate.addLabeledVertex(target);
    delegate.addLabeledEdge(sIdx, tIdx, edge);
  }
}
