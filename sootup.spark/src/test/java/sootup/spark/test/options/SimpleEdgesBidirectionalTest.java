package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PAGEdge;
import sootup.spark.SparkOptions;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.test.SparkTestUtil;

public class SimpleEdgesBidirectionalTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void testMethodToPAGBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    SparkOptions options = SparkOptions.builder().simpleEdgesBidirectional(true).build();
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig, options);

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    var newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    var fieldStack5 = SparkTestUtil.var(fieldType, "$stack5", mainSig);
    var newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);
    var containerStack6 = SparkTestUtil.var(containerType, "$stack6", mainSig);
    var fieldL2 = SparkTestUtil.var(fieldType, "l2", mainSig);
    var containerL3 = SparkTestUtil.var(containerType, "l3", mainSig);
    var containerL4 = SparkTestUtil.var(containerType, "l4", mainSig);
    var fieldRef =
        InstanceFieldRefNode.builder()
            .base(containerStack6)
            .field(SparkTestUtil.idFactory.getFieldSignature("field", containerType, fieldType))
            .type(fieldType)
            .containingMethodSig(mainSig)
            .build();

    assertTrue(delegate.containsEdge(newField, fieldStack5), "Edge new Field -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(newContainer, containerStack6),
        "Edge new Container -> $stack6 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldL2), "Edge $stack5 -> l2 not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL3), "Edge $stack6 -> l3 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldRef), "Edge $stack5 -> field ref not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL4), "Edge $stack6 -> l4 not found");

    // Reverse edges due to simpleEdgesBidirectional
    assertTrue(
        delegate.containsEdge(fieldStack5, newField),
        "Reverse edge $stack5 -> new Field not found");
    assertTrue(
        delegate.containsEdge(containerStack6, newContainer),
        "Reverse edge $stack6 -> new Container not found");
    assertTrue(delegate.containsEdge(fieldL2, fieldStack5), "Reverse edge l2 -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(containerL3, containerStack6),
        "Reverse edge l3 -> $stack6 not found");
    assertTrue(
        delegate.containsEdge(fieldRef, fieldStack5),
        "Reverse edge field ref -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(containerL4, containerStack6),
        "Reverse edge l4 -> $stack6 not found");
  }
}
