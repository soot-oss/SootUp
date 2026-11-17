package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;

/** Points-to sets and on-the-fly call edges for objects passed through static fields and arrays. */
public class StaticFieldsAndArraysTest {

  private final ClassType mainClass = SparkTestUtil.idFactory.getClassType("StaticAndArrays");
  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          mainClass, SparkTestUtil.idFactory.getMainSubSignature());

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  private Spark solve(boolean onTheFly) {
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(Collections.singletonList(mainSig))
        .sparkOptions(SparkOptions.builder().onFlyCallGraph(onTheFly).build())
        .build();
  }

  private Local local(String name) {
    Body body = SparkTestUtil.view.getMethod(mainSig).get().getBody();
    return body.getLocals().stream()
        .filter(l -> l.getName().equals(name))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no local " + name + " in " + body));
  }

  private Set<String> reachingTypes(PointsToAnalysis pta, String local) {
    return pta.reachingObjects(local(local), mainSig).stream()
        .map(o -> ((Type) o.getType()).toString())
        .collect(Collectors.toSet());
  }

  private Set<String> nameTargets(CallGraph cg) {
    return cg.callsFrom(mainSig).stream()
        .map(CallGraph.Call::targetMethodSignature)
        .filter(m -> m.getName().equals("name"))
        .map(m -> m.getDeclClassType().getClassName())
        .collect(Collectors.toSet());
  }

  @Test
  public void objectsFlowThroughStaticFields() {
    for (boolean onTheFly : new boolean[] {false, true}) {
      Engine.resetAllocCount();
      PointsToAnalysis pta = solve(onTheFly).getPointsToAnalysis();
      assertEquals(
          Collections.singleton("StaticAndArrays$Circle"),
          reachingTypes(pta, "fromStatic"),
          "onTheFly=" + onTheFly);
    }
  }

  @Test
  public void objectsFlowThroughArraysIndependentOfTheIndex() {
    for (boolean onTheFly : new boolean[] {false, true}) {
      Engine.resetAllocCount();
      PointsToAnalysis pta = solve(onTheFly).getPointsToAnalysis();
      assertEquals(
          Collections.singleton("StaticAndArrays$Shape[]"),
          reachingTypes(pta, "shapes"),
          "onTheFly=" + onTheFly);
      assertEquals(
          Collections.singleton("StaticAndArrays$Square"),
          reachingTypes(pta, "fromArray"),
          "onTheFly=" + onTheFly);
    }
  }

  @Test
  public void objectsFlowThroughTheInnerArraysOfMultiDimensionalArrays() {
    for (boolean onTheFly : new boolean[] {false, true}) {
      Engine.resetAllocCount();
      PointsToAnalysis pta = solve(onTheFly).getPointsToAnalysis();
      assertEquals(
          Collections.singleton("StaticAndArrays$Shape[][]"),
          reachingTypes(pta, "grid"),
          "onTheFly=" + onTheFly);
      assertEquals(
          Collections.singleton("StaticAndArrays$Triangle"),
          reachingTypes(pta, "fromGrid"),
          "onTheFly=" + onTheFly);
      assertEquals(
          Collections.singleton("StaticAndArrays$Triangle"),
          reachingTypes(pta, "fromCube"),
          "onTheFly=" + onTheFly);
    }
  }

  @Test
  public void onTheFlyCallGraphResolvesCallsOnSuchObjects() {
    CallGraph cg = solve(true).getCallGraph();
    assertEquals(
        new java.util.HashSet<>(
            java.util.Arrays.asList(
                "StaticAndArrays$Circle", "StaticAndArrays$Square", "StaticAndArrays$Triangle")),
        nameTargets(cg));
  }
}
