package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
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

  /**
   * Regression test: under OTF, {@code x = x.f} is applied via {@link
   * sootup.spark.IncrementalPointsToAnalysis#applyLoad}, which iterates {@code pts(x)} directly out
   * of the shared points-to map. Before the fix, growing {@code pts(x)} mid-iteration (because base
   * and target are the same local) threw {@link java.util.ConcurrentModificationException}.
   */
  @Test
  public void otfSelfReferentialFieldLoadDoesNotThrowConcurrentModificationException() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("SelfFieldLoad");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());

    SparkOptions options = SparkOptions.builder().onFlyCallGraph(true).build();
    PointsToAnalysis pta =
        assertDoesNotThrow(
            () ->
                Spark.builder()
                    .view(SparkTestUtil.view)
                    .entryPoints(Collections.singletonList(mainSig))
                    .sparkOptions(options)
                    .build()
                    .getPointsToAnalysis());

    ClassType nodeType = SparkTestUtil.idFactory.getClassType("SelfFieldLoad$Node");
    AllocationNode newA = SparkTestUtil.alloc(nodeType, 1L, mainSig);
    AllocationNode newB = SparkTestUtil.alloc(nodeType, 2L, mainSig);
    AllocationNode newC = SparkTestUtil.alloc(nodeType, 3L, mainSig);

    Set<AllocationNode> reachingX = pta.reachingObjects(new Local("l4", nodeType), mainSig);
    assertTrue(reachingX.containsAll(Set.of(newA, newB, newC)), reachingX::toString);
  }

  /**
   * Regression test: {@link sootup.spark.MethodPAGStmtVisitor#handleInvokeExprOtf} used to treat
   * every non-static/non-special invoke as a virtual call and queue it as a {@code
   * PendingVirtualCall}, including {@code invokedynamic} call sites (lambdas, method references).
   * {@link sootup.spark.Solver#solveOnTheFly} then force-cast the pending call's expr to {@link
   * sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr} to read the receiver, which throws
   * {@link ClassCastException} for a {@link sootup.core.jimple.common.expr.JDynamicInvokeExpr} (it
   * has no such receiver). Fixed by skipping {@code JDynamicInvokeExpr} in {@code
   * handleInvokeExprOtf}, matching {@link sootup.callgraph.ClassHierarchyAnalysisAlgorithm}'s
   * CHA-mode behavior of not resolving invokedynamic targets either.
   */
  @Test
  public void otfDoesNotCrashOnInvokedynamicCallSite() {
    ClassType mainClass = SparkTestUtil.idFactory.getClassType("LambdaCallSite");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            mainClass, SparkTestUtil.idFactory.getMainSubSignature());

    SparkOptions options = SparkOptions.builder().onFlyCallGraph(true).build();
    Spark spark =
        assertDoesNotThrow(
            () ->
                Spark.builder()
                    .view(SparkTestUtil.view)
                    .entryPoints(Collections.singletonList(mainSig))
                    .sparkOptions(options)
                    .build());

    CallGraph cg = assertDoesNotThrow(spark::getCallGraph);
    assertDoesNotThrow(spark::getPointsToAnalysis);

    // The invokedynamic call site must not derail resolution of the ordinary virtual call
    // alongside it in the same method.
    ClassType containerType = SparkTestUtil.idFactory.getClassType("LambdaCallSite$Container");
    ClassType valueType = SparkTestUtil.idFactory.getClassType("LambdaCallSite$Value");
    MethodSignature getValueSig =
        SparkTestUtil.idFactory.getMethodSignature(
            containerType, "getValue", valueType, List.of(valueType));
    assertTrue(cg.containsMethod(mainSig), "main must be in OTF call graph");
    assertTrue(cg.containsMethod(getValueSig), "OTF must still discover getValue via receiver pts");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(getValueSig), "main -> getValue edge must be present");
  }
}
