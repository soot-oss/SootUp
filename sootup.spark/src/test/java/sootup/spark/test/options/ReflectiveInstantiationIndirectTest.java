package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Local;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.test.SparkTestUtil;

/**
 * Covers the interprocedural extension to {@link sootup.spark.MethodPAGStmtVisitor}'s {@code
 * Class.forName} handling: a literal class name reaching {@code Class.forName} through one or more
 * levels of parameter-passing (not just directly at the call site, as {@link
 * ReflectiveInstantiationTest} covers) must still resolve, via {@code StringConstantNode}s riding
 * the same PAG edges as any other allocation. Mirrors the JAXP {@code
 * TransformerFactory.newInstance(String, ClassLoader)} -> {@code FactoryFinder.newInstance(...)} ->
 * {@code Class.forName(...)} shape that motivated this feature.
 */
public class ReflectiveInstantiationIndirectTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("ReflectiveInstantiationIndirect");
  ClassType widgetType =
      SparkTestUtil.idFactory.getClassType("ReflectiveInstantiationIndirect$Widget");
  ClassType objectType = SparkTestUtil.idFactory.getClassType("java.lang.Object");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  MethodSignature widgetCtorSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "<init>", VoidType.getInstance(), Collections.emptyList());

  SparkOptions otfOptions = SparkOptions.builder().onFlyCallGraph(true).build();

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void literalReachingClassForNameThroughOneWrapperHopIsResolved() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();
    assertTrue(
        cg.containsMethod(widgetCtorSig),
        "Widget's constructor must be reachable via a literal forwarded one hop into "
            + "Class.forName");

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    Set<AllocationNode> viaOneHop = pta.reachingObjects(new Local("l1", objectType), mainSig);
    assertTrue(
        viaOneHop.stream().anyMatch(a -> a.getType().equals(widgetType)),
        () ->
            "createViaLiteral(\"Widget\")'s newInstance() result must point to a fresh Widget "
                + "allocation: "
                + viaOneHop);
  }

  @Test
  public void literalReachingClassForNameThroughTwoWrapperHopsIsResolved() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    Set<AllocationNode> viaTwoHops = pta.reachingObjects(new Local("l2", objectType), mainSig);
    assertTrue(
        viaTwoHops.stream().anyMatch(a -> a.getType().equals(widgetType)),
        () ->
            "createViaWrapper(\"Widget\")'s newInstance() result (literal forwarded through two "
                + "wrapper hops before reaching Class.forName) must point to a fresh Widget "
                + "allocation: "
                + viaTwoHops);
  }

  @Test
  public void computedNameForwardedThroughWrapperIsStillNotResolved() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    Set<AllocationNode> viaComputedName = pta.reachingObjects(new Local("l3", objectType), mainSig);
    assertTrue(
        viaComputedName.isEmpty(),
        () ->
            "createViaWrapper(args[0]) is not a literal at any point in the chain, so "
                + "newInstance() on it must not resolve to anything: "
                + viaComputedName);
  }
}
