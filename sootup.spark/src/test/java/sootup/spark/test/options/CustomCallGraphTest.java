package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.test.SparkTestUtil;

public class CustomCallGraphTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("InstanceMethodCall");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  ClassType containerType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Container");
  ClassType valueType = SparkTestUtil.idFactory.getClassType("InstanceMethodCall$Value");
  MethodSignature getValueSig =
      SparkTestUtil.idFactory.getMethodSignature(
          containerType, "getValue", valueType, List.of(valueType));

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  /**
   * A client-supplied call graph must be used as-is (no CHA re-run) and drive PAG construction the
   * same way Spark's own CHA-computed call graph would.
   */
  @Test
  public void clientSuppliedCallGraphIsUsedInPlaceOfCha() {
    CallGraph clientCg =
        new ClassHierarchyAnalysisAlgorithm(SparkTestUtil.view)
            .initialize(Collections.singletonList(mainSig));

    Spark spark = Spark.builder().view(SparkTestUtil.view).callGraph(clientCg).build();

    assertSame(clientCg, spark.getCallGraph(), "Spark must expose the supplied call graph as-is");
    assertTrue(
        spark.getCallGraph().containsMethod(getValueSig),
        "the client-built CHA graph must already contain the resolved virtual callee");

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    AllocationNode valueAlloc = SparkTestUtil.alloc(valueType, 2L, mainSig);
    assertTrue(
        pta.reachingObjects(new Local("l2", valueType), mainSig).contains(valueAlloc)
            || pta.reachingObjects(new Local("l3", valueType), mainSig).contains(valueAlloc),
        "PAG built from the client-supplied call graph must still resolve the virtual call");
  }

  /** Sanity check that the client-built CHA graph and Spark's own CHA path agree on membership. */
  @Test
  public void clientSuppliedCallGraphMatchesSparksOwnCha() {
    CallGraph clientCg =
        new ClassHierarchyAnalysisAlgorithm(SparkTestUtil.view)
            .initialize(Collections.singletonList(mainSig));
    Spark clientDriven = Spark.builder().view(SparkTestUtil.view).callGraph(clientCg).build();

    Spark sparkDriven =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .build();

    assertEquals(
        sparkDriven.getCallGraph().getMethodSignatures(),
        clientDriven.getCallGraph().getMethodSignatures());
  }

  /**
   * A call graph supplied alongside {@code onFlyCallGraph} is incompatible with OTF's incremental
   * construction, so it must be discarded (with a warning) rather than used or rejected outright.
   */
  @Test
  public void callGraphSuppliedWithOnFlyCallGraphIsDiscarded() {
    CallGraph clientCg =
        new ClassHierarchyAnalysisAlgorithm(SparkTestUtil.view)
            .initialize(Collections.singletonList(mainSig));
    SparkOptions options = SparkOptions.builder().onFlyCallGraph(true).build();

    Spark spark =
        assertDoesNotThrow(
            () ->
                Spark.builder()
                    .view(SparkTestUtil.view)
                    .entryPoints(Collections.singletonList(mainSig))
                    .sparkOptions(options)
                    .callGraph(clientCg)
                    .build());

    assertNotSame(
        clientCg,
        spark.getCallGraph(),
        "the discarded call graph must not be exposed; OTF must build its own instead");
    assertTrue(
        spark.getCallGraph().containsMethod(mainSig), "OTF-built call graph must still work");
  }

  @Test
  public void missingBothEntryPointsAndCallGraphThrows() {
    assertThrows(
        IllegalArgumentException.class, () -> Spark.builder().view(SparkTestUtil.view).build());
  }

  /**
   * {@code entryPoints} being present but empty is a distinct failure mode from it being {@code
   * null} entirely — both must be rejected since neither can drive CHA/OTF construction.
   */
  @Test
  public void emptyEntryPointsWithoutCallGraphThrows() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            Spark.builder().view(SparkTestUtil.view).entryPoints(Collections.emptyList()).build());
  }
}
