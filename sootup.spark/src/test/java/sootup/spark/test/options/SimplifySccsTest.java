package sootup.spark.test.options;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.Type;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.Node;
import sootup.spark.node.VariableNode;
import sootup.spark.test.SparkTestUtil;

public class SimplifySccsTest {

  private final MethodSignature mainSig =
      SparkTestUtil.idFactory.getMethodSignature(
          SparkTestUtil.idFactory.getClassType("SimplifySccs"),
          SparkTestUtil.idFactory.getMainSubSignature());

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  private PointsToAnalysis pointsTo(boolean simplifySccs) {
    Engine.resetAllocCount();
    return Spark.builder()
        .view(SparkTestUtil.view)
        .entryPoints(List.of(mainSig))
        .sparkOptions(SparkOptions.builder().simplifySccs(simplifySccs).build())
        .build()
        .getPointsToAnalysis();
  }

  /** The types each node points to */
  private static Map<Node, Set<Type>> types(PointsToAnalysis pta) {
    return pta.getPointsToMap().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e ->
                    e.getValue().stream()
                        .map(AllocationNode::getType)
                        .collect(Collectors.toSet())));
  }

  /** The distinct points-to sets of the variables of main */
  private static Set<Set<AllocationNode>> distinctSetsOfMain(PointsToAnalysis pta) {
    Set<Set<AllocationNode>> sets = Collections.newSetFromMap(new IdentityHashMap<>());
    pta.getPointsToMap()
        .forEach(
            (node, pts) -> {
              if (node instanceof VariableNode
                  && node.getContainingMethodSig().getName().equals("main")) {
                sets.add(pts);
              }
            });
    return sets;
  }

  @Test
  public void sameResults() {
    Map<Node, Set<Type>> withoutCollapsing = types(pointsTo(false));
    Map<Node, Set<Type>> withCollapsing = types(pointsTo(true));
    assertFalse(withoutCollapsing.isEmpty());
    assertEquals(withoutCollapsing, withCollapsing);
  }

  @Test
  public void cycleSharesOneSet() {
    // without collapsing, each variable has its own set
    PointsToAnalysis plain = pointsTo(false);
    PointsToAnalysis collapsed = pointsTo(true);
    assertTrue(distinctSetsOfMain(collapsed).size() < distinctSetsOfMain(plain).size());
  }
}
