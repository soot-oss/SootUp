package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import org.graph4j.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.spark.Engine;
import sootup.spark.PAGEdge;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;
import sootup.spark.node.StaticFieldRefNode;
import sootup.spark.test.SparkTestUtil;

public class IgnoreBaseObjectsTest {

  ClassType classSig = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          classSig, SparkTestUtil.idFactory.getMainSubSignature());

  ClassType containerType = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects$Container");
  ClassType valueType = SparkTestUtil.idFactory.getClassType("IgnoreBaseObjects$Value");
  FieldSignature valueField =
      new JavaIdentifierFactory().getFieldSignature("value", containerType, valueType);

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
    assertTrue(
        SparkTestUtil.containsVertex(delegate, fieldRefStack9), "$stack9.value node missing");
    assertTrue(
        SparkTestUtil.containsVertex(delegate, fieldRefStack10), "$stack10.value node missing");

    // Each store goes to its own field ref node
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, SparkTestUtil.var(valueType, "$stack7", mainSig), fieldRefStack9),
        "$stack7 -> $stack9.value missing");
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, SparkTestUtil.var(valueType, "$stack8", mainSig), fieldRefStack10),
        "$stack8 -> $stack10.value missing");

    // Each load comes from its own field ref node
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, fieldRefStack9, SparkTestUtil.var(valueType, "l5", mainSig)),
        "$stack9.value -> l5 missing");
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, fieldRefStack10, SparkTestUtil.var(valueType, "l6", mainSig)),
        "$stack10.value -> l6 missing");
  }

  @Test
  public void testFieldBased() {
    SparkOptions options = SparkOptions.builder().ignoreBaseObjects(true).build();
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig, options);

    // the field is one global variable, whatever the base
    StaticFieldRefNode field =
        StaticFieldRefNode.builder()
            .field(valueField)
            .type(valueType)
            .containingMethodSig(SparkTestUtil.GLOBAL_SCOPE)
            .build();
    assertTrue(SparkTestUtil.containsVertex(delegate, field), "global field node missing");

    // Both stores converge on the global node
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, SparkTestUtil.var(valueType, "$stack7", mainSig), field),
        "$stack7 -> value missing");
    assertTrue(
        SparkTestUtil.containsEdge(
            delegate, SparkTestUtil.var(valueType, "$stack8", mainSig), field),
        "$stack8 -> value missing");

    // Both loads come from the global node
    assertTrue(
        SparkTestUtil.containsEdge(delegate, field, SparkTestUtil.var(valueType, "l5", mainSig)),
        "value -> l5 missing");
    assertTrue(
        SparkTestUtil.containsEdge(delegate, field, SparkTestUtil.var(valueType, "l6", mainSig)),
        "value -> l6 missing");
  }

  private PointsToAnalysis pointsTo(SparkOptions options) {
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .sparkOptions(options)
        .build()
        .getPointsToAnalysis();
  }

  @Test
  public void fieldBasedLoadsSeeTheStoresOfAllBases() {
    PointsToAnalysis pta = pointsTo(SparkOptions.builder().ignoreBaseObjects(true).build());
    Set<AllocationNode> both =
        Set.of(
            SparkTestUtil.alloc(valueType, 1L, mainSig),
            SparkTestUtil.alloc(valueType, 2L, mainSig));
    assertEquals(both, pta.reachingObjects(Jimple.newLocal("l5", valueType), mainSig));
    assertEquals(both, pta.reachingObjects(Jimple.newLocal("l6", valueType), mainSig));
    // a field query sees the global variable, whatever the objects
    assertEquals(
        both,
        pta.reachingObjects(Set.of(SparkTestUtil.alloc(containerType, 3L, mainSig)), valueField));
  }

  @Test
  public void fieldSensitiveLoadsSeeTheStoresOfTheirBase() {
    PointsToAnalysis pta = pointsTo(SparkOptions.defaultOptions());
    assertEquals(
        Set.of(SparkTestUtil.alloc(valueType, 1L, mainSig)),
        pta.reachingObjects(Jimple.newLocal("l5", valueType), mainSig));
    assertEquals(
        Set.of(SparkTestUtil.alloc(valueType, 2L, mainSig)),
        pta.reachingObjects(Jimple.newLocal("l6", valueType), mainSig));
  }
}
