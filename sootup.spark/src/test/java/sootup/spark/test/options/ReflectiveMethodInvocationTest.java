package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.test.SparkTestUtil;

/**
 * Covers {@link sootup.spark.Solver}'s {@code Class#getMethod(String, Class[])} / {@code
 * Class#getDeclaredMethod(String, Class[])} + {@code Method#invoke(Object, Object[])} resolution,
 * and its constructor-arity fix (a single-arg constructor reflectively invoked via {@code
 * getConstructor(int.class).newInstance(...)} must resolve, not just the no-arg case) -- the exact
 * shape DaCapo's own {@code org.dacapo.harness.*} driver classes use to reach their wrapped
 * benchmark implementations.
 */
public class ReflectiveMethodInvocationTest {

  ClassType mainClass = SparkTestUtil.idFactory.getClassType("ReflectiveMethodInvocation");
  ClassType widgetType = SparkTestUtil.idFactory.getClassType("ReflectiveMethodInvocation$Widget");
  MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());
  MethodSignature widgetCtorSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType,
          "<init>",
          VoidType.getInstance(),
          Collections.singletonList(PrimitiveType.getInt()));
  MethodSignature ctorTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "ctorTouch", VoidType.getInstance(), Collections.emptyList());
  MethodSignature doWorkSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "doWork", VoidType.getInstance(), Collections.emptyList());
  MethodSignature workTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "workTouch", VoidType.getInstance(), Collections.emptyList());
  MethodSignature secretWorkSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "secretWork", VoidType.getInstance(), Collections.emptyList());
  MethodSignature secretTouchSig =
      SparkTestUtil.idFactory.getMethodSignature(
          widgetType, "secretTouch", VoidType.getInstance(), Collections.emptyList());

  SparkOptions otfOptions = SparkOptions.builder().onFlyCallGraph(true).build();

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void reflectivelyInvokedMethodsAreReachableThroughSingleArgConstructor() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();

    assertTrue(
        cg.containsMethod(widgetCtorSig),
        "Widget's single-arg constructor must be reachable via getConstructor(int.class)"
            + ".newInstance(42), not just a no-arg constructor");
    assertTrue(
        cg.containsMethod(ctorTouchSig) && cg.callTargetsFrom(widgetCtorSig).contains(ctorTouchSig),
        "the resolved constructor's body must actually be analyzed");

    assertTrue(
        cg.containsMethod(doWorkSig),
        "doWork() must be reachable via getMethod(\"doWork\").invoke");
    assertTrue(
        cg.callTargetsFrom(doWorkSig).contains(workTouchSig),
        "doWork()'s body must actually be analyzed");

    assertTrue(
        cg.containsMethod(secretWorkSig),
        "secretWork() must be reachable via getDeclaredMethod(\"secretWork\").invoke");
    assertTrue(
        cg.callTargetsFrom(secretWorkSig).contains(secretTouchSig),
        "secretWork()'s body must actually be analyzed");
  }

  @Test
  public void computedMethodNameIsNotResolved() {
    Spark spark =
        Spark.builder()
            .view(SparkTestUtil.view)
            .entryPoints(Collections.singletonList(mainSig))
            .sparkOptions(otfOptions)
            .build();

    CallGraph cg = spark.getCallGraph();
    // Widget declares exactly 6 methods total. getMethod(args[0]) is not a literal, so
    // invoke() on its result must not resolve to anything -- if it spuriously matched some
    // other declared method by accident, the reachable set below would still be exactly these
    // 6 (they're already reachable via the literal-named calls), so this alone wouldn't catch
    // a regression; the real guarantee is that Widget has no 7th method for it to reach.
    Set<MethodSignature> reachableWidgetMethods =
        cg.getMethodSignatures().stream()
            .filter(sig -> sig.getDeclClassType().equals(widgetType))
            .collect(Collectors.toSet());
    assertEquals(
        Set.of(widgetCtorSig, ctorTouchSig, doWorkSig, workTouchSig, secretWorkSig, secretTouchSig),
        reachableWidgetMethods,
        "Widget declares exactly 6 methods; a computed getMethod() name must not make any "
            + "further one reachable");
  }
}
