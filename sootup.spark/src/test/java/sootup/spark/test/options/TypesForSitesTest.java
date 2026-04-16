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
import sootup.spark.node.AllocationNode;
import sootup.spark.node.Node;
import sootup.spark.test.SparkTestUtil;

public class TypesForSitesTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void testMethodToPAGMultiAllocSameType() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("MultiAllocSameType");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    SparkOptions options = SparkOptions.builder().typesForSites(true).build();
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig, options);

    ClassType oType = SparkTestUtil.idFactory.getClassType("MultiAllocSameType$O");

    // Single allocation node for all allocations of the same type (no allocationSite)
    var newO = AllocationNode.builder().type(oType).containingMethodSig(mainSig).build();

    // All three stack variables point to the single allocation node
    assertTrue(
        delegate.containsEdge(newO, SparkTestUtil.var(oType, "$stack4", mainSig)),
        "Edge new O -> $stack4 not found");
    assertTrue(
        delegate.containsEdge(newO, SparkTestUtil.var(oType, "$stack5", mainSig)),
        "Edge new O -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(newO, SparkTestUtil.var(oType, "$stack6", mainSig)),
        "Edge new O -> $stack6 not found");

    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack4", mainSig), SparkTestUtil.var(oType, "l1", mainSig)),
        "Edge $stack4 -> l1 not found");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack5", mainSig), SparkTestUtil.var(oType, "l2", mainSig)),
        "Edge $stack5 -> l2 not found");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack6", mainSig), SparkTestUtil.var(oType, "l3", mainSig)),
        "Edge $stack6 -> l3 not found");
  }
}
