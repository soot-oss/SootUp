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
 * Covers {@link sootup.spark.MethodPAGStmtVisitor}'s {@code Class.forName("literal")} handling and
 * {@link sootup.spark.Solver}'s reflective {@code newInstance()} /{@code
 * getConstructor().newInstance()} resolution: a reflectively-instantiated class must become
 * reachable and analyzed exactly as if the source had written {@code new Widget()} directly, but
 * only when the class name is a compile-time string literal -- a computed name must resolve to
 * nothing, same as before this feature existed.
 */
public class ReflectiveInstantiationTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("ReflectiveInstantiation");
  ClassType widgetType = SparkTestUtil.idFactory.getClassType("ReflectiveInstantiation$Widget");
  ClassType objectType = SparkTestUtil.idFactory.getClassType("java.lang.Object");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  MethodSignature widgetCtorSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "<init>", VoidType.getInstance(), Collections.emptyList());
  MethodSignature widgetTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "touch", VoidType.getInstance(), Collections.emptyList());

  SparkOptions otfOptions = SparkOptions.builder().onFlyCallGraph(true).build();

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void reflectivelyInstantiatedClassIsReachableAndCaptureFlows() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();
    assertTrue(
        cg.containsMethod(widgetCtorSig), "Widget's constructor must be reachable via forName");
    assertTrue(
        cg.callTargetsFrom(mainSig).contains(widgetCtorSig),
        "main -> Widget.<init>() edge must exist");
    assertTrue(
        cg.containsMethod(widgetTouchSig),
        "touch() is only reachable if the constructor's body was actually analyzed, not just "
            + "registered as a call graph node");
    assertTrue(
        cg.callTargetsFrom(widgetCtorSig).contains(widgetTouchSig),
        "Widget.<init>() -> touch() edge must exist");

    PointsToAnalysis pta = spark.getPointsToAnalysis();

    Set<AllocationNode> viaNewInstance = pta.reachingObjects(new Local("l2", objectType), mainSig);
    assertTrue(
        viaNewInstance.stream().anyMatch(a -> a.getType().equals(widgetType)),
        () ->
            "Class#newInstance()'s result must point to a fresh Widget allocation: "
                + viaNewInstance);

    Set<AllocationNode> viaCtorChain = pta.reachingObjects(new Local("l4", objectType), mainSig);
    assertTrue(
        viaCtorChain.stream().anyMatch(a -> a.getType().equals(widgetType)),
        () ->
            "Class#getDeclaredConstructor().newInstance()'s result must point to a fresh Widget "
                + "allocation: "
                + viaCtorChain);
  }

  @Test
  public void computedClassNameIsNotResolved() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    PointsToAnalysis pta = spark.getPointsToAnalysis();
    Set<AllocationNode> viaComputedName = pta.reachingObjects(new Local("l6", objectType), mainSig);
    assertTrue(
        viaComputedName.isEmpty(),
        () ->
            "Class.forName(args[0]) is not a literal, so newInstance() on it must not resolve "
                + "to anything: "
                + viaComputedName);
  }
}
