package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.*;

import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PAGEdge;
import sootup.spark.SparkOptions;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.test.SparkTestUtil;

public class IgnoreBaseObjectsTest {

  ClassType classSig = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          classSig, SparkTestUtil.idFactory.getMainSubSignature());

  ClassType containerType = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects$Container");
  ClassType valueType = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects$Value");
  FieldSignature valueField = new FieldSignature(containerType, "value", valueType);

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void testFieldSensitive() {
    SparkOptions options = SparkOptions.builder().build();
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig, options);

    // Two distinct field ref nodes — one per base variable
    InstanceFieldRefNode fieldRefStack9 =
        SparkTestUtil.fieldRef(
            SparkTestUtil.var(containerType, "$stack9", mainSig), valueField, valueType, mainSig);
    InstanceFieldRefNode fieldRefStack10 =
        SparkTestUtil.fieldRef(
            SparkTestUtil.var(containerType, "$stack10", mainSig), valueField, valueType, mainSig);

    assertNotEquals(fieldRefStack9, fieldRefStack10, "Field ref nodes must be distinct");
    assertTrue(delegate.containsVertex(fieldRefStack9), "$stack9.value node missing");
    assertTrue(delegate.containsVertex(fieldRefStack10), "$stack10.value node missing");

    // Each store goes to its own field ref node
    assertTrue(
        delegate.containsEdge(SparkTestUtil.var(valueType, "$stack7", mainSig), fieldRefStack9),
        "$stack7 -> $stack9.value missing");
    assertTrue(
        delegate.containsEdge(SparkTestUtil.var(valueType, "$stack8", mainSig), fieldRefStack10),
        "$stack8 -> $stack10.value missing");

    // Each load comes from its own field ref node
    assertTrue(
        delegate.containsEdge(fieldRefStack9, SparkTestUtil.var(valueType, "l5", mainSig)),
        "$stack9.value -> l5 missing");
    assertTrue(
        delegate.containsEdge(fieldRefStack10, SparkTestUtil.var(valueType, "l6", mainSig)),
        "$stack10.value -> l6 missing");
  }

  @Test
  public void testFieldBased() {
    SparkOptions options = SparkOptions.builder().ignoreBaseObjects(true).build();
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig, options);

    // Single merged field ref node keyed by base type, scoped to GLOBAL_SCOPE
    InstanceFieldRefNode mergedFieldRef =
        SparkTestUtil.fieldRef(
            SparkTestUtil.var(containerType, containerType.toString(), SparkTestUtil.GLOBAL_SCOPE),
            valueField,
            valueType,
            SparkTestUtil.GLOBAL_SCOPE);

    assertTrue(delegate.containsVertex(mergedFieldRef), "Merged field ref node missing");

    // Both stores converge on the single merged node
    assertTrue(
        delegate.containsEdge(SparkTestUtil.var(valueType, "$stack7", mainSig), mergedFieldRef),
        "$stack7 -> merged.value missing");
    assertTrue(
        delegate.containsEdge(SparkTestUtil.var(valueType, "$stack8", mainSig), mergedFieldRef),
        "$stack8 -> merged.value missing");

    // Both loads come from the same merged node
    assertTrue(
        delegate.containsEdge(mergedFieldRef, SparkTestUtil.var(valueType, "l5", mainSig)),
        "merged.value -> l5 missing");
    assertTrue(
        delegate.containsEdge(mergedFieldRef, SparkTestUtil.var(valueType, "l6", mainSig)),
        "merged.value -> l6 missing");
  }
}
