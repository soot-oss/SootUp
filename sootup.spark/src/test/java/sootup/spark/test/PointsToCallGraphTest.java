package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;

/**
 * Without an on-the-fly call graph, SPARK builds the PAG on a CHA call graph; the call graph it
 * returns must be the one the points-to sets give, as with an on-the-fly call graph.
 */
public class PointsToCallGraphTest {

  private final ClassType mainType = SparkTestUtil.idFactory.getClassType("PointsToCallGraph");
  private final ClassType valueType =
      SparkTestUtil.idFactory.getClassType("PointsToCallGraph$Value");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainType, SparkTestUtil.idFactory.getMainSubSignature());
  private final MethodSignature circleArea = area("PointsToCallGraph$Circle");
  private final MethodSignature squareArea = area("PointsToCallGraph$Square");
  private final MethodSignature onlyFromSquare =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("PointsToCallGraph$OnlyFromSquare"),
          "make",
          valueType,
          List.of());
  private final MethodSignature utilId =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("PointsToCallGraph$Util"),
          "id",
          valueType,
          List.of(valueType));

  private MethodSignature area(String className) {
    return SparkTestUtil.idFactory.getMethodSignature(
        SparkTestUtil.idFactory.getClassType(className), "area", valueType, List.of());
  }

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  private CallGraph sparkCallGraph(boolean onFly) {
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(onFly).build())
        .build()
        .getCallGraph();
  }

  @Test
  public void virtualCallKeepsOnlyTheTargetsOfThePointedToObjects() {
    CallGraph cha =
        new ClassHierarchyAnalysisAlgorithm(SparkTestUtil.view).initialize(List.of(mainSig));
    assertTrue(cha.callTargetsFrom(mainSig).contains(squareArea));
    assertTrue(cha.containsMethod(onlyFromSquare));

    CallGraph cg = sparkCallGraph(false);
    assertTrue(cg.callTargetsFrom(mainSig).contains(circleArea));
    assertFalse(cg.callTargetsFrom(mainSig).contains(squareArea));
    // only reachable through the dropped call
    assertFalse(cg.containsMethod(onlyFromSquare));
    // static calls are kept
    assertTrue(cg.callTargetsFrom(mainSig).contains(utilId));
  }

  @Test
  public void sameCallsAsTheOnTheFlyCallGraph() {
    assertEquals(edges(sparkCallGraph(true)), edges(sparkCallGraph(false)));
  }

  private static Set<List<MethodSignature>> edges(CallGraph cg) {
    return cg.getCalls().stream()
        .map(c -> List.of(c.sourceMethodSignature(), c.targetMethodSignature()))
        .collect(Collectors.toSet());
  }
}
