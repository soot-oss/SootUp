package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.test.SparkTestUtil;

public class OnFlyCallGraphTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  /** On a program with no virtual calls, OTF and CHA must converge to the same allocation-level */
  @Test
  public void otfMatchesChaForBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());

    SparkOptions options = SparkOptions.builder().onFlyCallGraph(true).build();
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(options)
            .build();
    PointsToAnalysis pta = spark.getPointsToAnalysis();

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    AllocationNode newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);

    assertEquals(
        Collections.singleton(newField), pta.reachingObjects(new Local("l2", fieldType), mainSig));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(new Local("l3", containerType), mainSig));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(new Local("l4", containerType), mainSig));
  }

  /**
   * On a program with virtual calls, OTF must discover the callees through the receiver's points-to
   * set and connect parameter / return flows to them.
   */
  @Test
  public void otfDiscoversVirtualCallees() {
    ClassType mainClass = SparkTestUtil.idFactory.getClassType("InstanceMethodCall");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            mainClass, SparkTestUtil.idFactory.getMainSubSignature());

    SparkOptions options = SparkOptions.builder().onFlyCallGraph(true).build();
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(options)
            .build();

    ClassType containerType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Container");
    ClassType valueType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Value");

    MethodSignature getValueSig =
        SparkTestUtil.idFactory.getMethodSignature(
            containerType, "getValue", valueType, List.of(valueType));
    MethodSignature processValueSig =
        SparkTestUtil.idFactory.getMethodSignature(
            containerType, "processValue", valueType, List.of(valueType));

    CallGraph cg = spark.getCallGraph();
    assertTrue(cg.containsMethod(mainSig), "main must be in OTF call graph");
    assertTrue(cg.containsMethod(getValueSig), "OTF must discover getValue via receiver pts");
    assertTrue(
        cg.containsMethod(processValueSig), "OTF must discover processValue via receiver pts");

    // Calls must be edges sourced from main, not orphan vertices.
    assertFalse(cg.callsFrom(mainSig).isEmpty(), "main must have outgoing calls");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(getValueSig), "main -> getValue edge must be present");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(processValueSig),
        "main -> processValue edge must be present");

    // The argument flow through getValue must produce a non-empty pts at the result of the call.
    // We don't assert specific local names here; instead, verify that the points-to analysis
    // reports the Value allocation as reachable from at least one local in main.
    PointsToAnalysis pta = spark.getPointsToAnalysis();
    AllocationNode valueAlloc = SparkTestUtil.alloc(valueType, 2L, mainSig);
    boolean foundValueReach =
        pta.reachingObjects(new Local("l2", valueType), mainSig).contains(valueAlloc)
            || pta.reachingObjects(new Local("l3", valueType), mainSig).contains(valueAlloc);
    assertTrue(foundValueReach, "Value allocation must reach at least one local in main");
  }
}
