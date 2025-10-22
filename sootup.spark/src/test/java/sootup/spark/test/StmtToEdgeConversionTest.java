package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.graph4j.Digraph;
import org.graph4j.Edge;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.spark.NodeFactory;
import sootup.spark.PAG;
import sootup.spark.PAGEdge;
import sootup.spark.PAGStmtVisitor;
import sootup.spark.node.Node;

public class StmtToEdgeConversionTest {

  ClassType aType = SparkTestUtil.simpleType("A");
  ClassType bType = SparkTestUtil.simpleType("B");
  FieldSignature fieldSig =
      JavaIdentifierFactory.getInstance().getFieldSignature("f", aType, bType);

  @Test
  public void testAllocEdge() {
    LValue left = new Local("a", aType);
    Value right = new JNewExpr(SparkTestUtil.simpleType("A"));
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.ALLOCATION);

    val source = methodPAG.getDelegate().getVertexLabel(edge.source());
    val target = methodPAG.getDelegate().getVertexLabel(edge.target());
    val expectedTarget = NodeFactory.createNode(left).get();
    val expectedSource = NodeFactory.createNode(right).get();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testAssignEdge() {
    LValue left = new Local("b", aType);
    Value right = new Local("a", aType);
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.ASSIGNMENT);

    val source = methodPAG.getDelegate().getVertexLabel(edge.source());
    val target = methodPAG.getDelegate().getVertexLabel(edge.target());
    val expectedTarget = NodeFactory.createNode(left).get();
    val expectedSource = NodeFactory.createNode(right).get();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testStoreEdge() {
    val aType = SparkTestUtil.simpleType("A");
    val base = new Local("someB", aType);
    val right = new JInstanceFieldRef(base, fieldSig);
    LValue left = new Local("b", aType);
    val methodPAG = new PAG();
    Edge edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.LOAD);

    val source = methodPAG.getDelegate().getVertexLabel(edge.source());
    val target = methodPAG.getDelegate().getVertexLabel(edge.target());
    Node expectedTarget = NodeFactory.createNode(left).get();
    Node expectedSource = NodeFactory.createNode(right).get();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  @Test
  public void testLoadEdge() {
    val aType = SparkTestUtil.simpleType("A");
    val base = new Local("someB", aType);
    val left = new JInstanceFieldRef(base, fieldSig);
    LValue right = new Local("b", aType);
    val methodPAG = new PAG();
    val edge = doAssignment(right, left, methodPAG, PAGEdge.EdgeType.STORE);

    val source = methodPAG.getDelegate().getVertexLabel(edge.source());
    val target = methodPAG.getDelegate().getVertexLabel(edge.target());
    Node expectedTarget = NodeFactory.createNode(left).get();
    Node expectedSource = NodeFactory.createNode(right).get();
    assertEquals(expectedTarget, target);
    assertEquals(expectedSource, source);
  }

  private Edge doAssignment(Value right, LValue left, PAG methodPAG, PAGEdge.EdgeType edgeType) {
    JAssignStmt assignStmt = new JAssignStmt(left, right, StmtPositionInfo.getNoStmtPositionInfo());
    assignStmt.accept(PAGStmtVisitor.builder().PAG(methodPAG).build());

    Edge foundEdge = null;
    Digraph<Node, PAGEdge> graph = methodPAG.getDelegate();
    for (Edge edge : graph.edges()) {
      PAGEdge e = graph.getEdgeLabel(edge.source(), edge.target());
      if (edgeType.equals(e.getEdgeType())) {
        foundEdge = edge;
        break; // stop at the first match
      }
    }
    assertTrue(foundEdge != null);
    return foundEdge;
  }
}
