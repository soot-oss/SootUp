package sootup.codepropertygraph.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.*;

public class GraphAssertionHelper {

  public static void assertNodesEqual(
      List<PropertyGraphNode> expectedNodes, List<PropertyGraphNode> actualNodes) {
    assertEquals(expectedNodes.size(), actualNodes.size(), "Node count should be equal");
    for (PropertyGraphNode expectedNode : expectedNodes) {
      assertTrue(
          actualNodes.stream().anyMatch(actualNode -> nodesAreEquivalent(expectedNode, actualNode)),
          "Expected node not found: " + expectedNode.toString());
    }
  }

  private static boolean nodesAreEquivalent(
      PropertyGraphNode expectedNode, PropertyGraphNode actualNode) {
    if (expectedNode.getClass() != actualNode.getClass()) {
      return false;
    }
    if (expectedNode instanceof MethodGraphNode) {
      return ((MethodGraphNode) expectedNode)
          .getMethod()
          .getSignature()
          .toString()
          .equals(((MethodGraphNode) actualNode).getMethod().getSignature().toString());
    } else if (expectedNode instanceof ExprGraphNode) {
      return ((ExprGraphNode) expectedNode)
          .getExpr()
          .equivTo(((ExprGraphNode) actualNode).getExpr());
    } else if (expectedNode instanceof StmtGraphNode) {
      return ((StmtGraphNode) expectedNode)
          .getStmt()
          .equivTo(((StmtGraphNode) actualNode).getStmt());
    } else if (expectedNode instanceof ImmediateGraphNode) {
      return ((ImmediateGraphNode) expectedNode)
          .getImmediate()
          .equivTo(((ImmediateGraphNode) actualNode).getImmediate());
    } else if (expectedNode instanceof TypeGraphNode) {
      return ((TypeGraphNode) expectedNode)
          .getType()
          .equals(((TypeGraphNode) actualNode).getType());
    } else if (expectedNode instanceof ModifierGraphNode) {
      return ((ModifierGraphNode) expectedNode)
          .getModifier()
          .equals(((ModifierGraphNode) actualNode).getModifier());
    } else if (expectedNode instanceof AggregateGraphNode) {
      return ((AggregateGraphNode) expectedNode)
          .getName()
          .equals(((AggregateGraphNode) actualNode).getName());
    }
    return expectedNode.toString().equals(actualNode.toString());
  }

  public static void assertEdgesEqual(
      List<PropertyGraphEdge> expectedEdges, List<PropertyGraphEdge> actualEdges) {
    assertEquals(expectedEdges.size(), actualEdges.size(), "Edge count should be equal");
    for (PropertyGraphEdge expectedEdge : expectedEdges) {
      assertTrue(
          actualEdges.stream().anyMatch(edge -> edgesAreEquivalent(expectedEdge, edge)),
          "Expected edge not found: " + expectedEdge.toString());
    }
  }

  private static boolean edgesAreEquivalent(
      PropertyGraphEdge expectedEdge, PropertyGraphEdge actualEdge) {
    return expectedEdge.getClass() == actualEdge.getClass()
        && nodesAreEquivalent(expectedEdge.getSource(), actualEdge.getSource())
        && nodesAreEquivalent(expectedEdge.getDestination(), actualEdge.getDestination());
  }

  public static void assertGraphEqual(PropertyGraph expected, PropertyGraph actual) {
    assertNodesEqual(expected.getNodes(), actual.getNodes());
    assertEdgesEqual(expected.getEdges(), actual.getEdges());
  }
}
