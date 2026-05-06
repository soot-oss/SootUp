package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Solver;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;

public class PointsToAnalysisTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void reachingObjectsAndTypesBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Solver solver = SparkTestUtil.solveMainWithSolver(mainSig);
    PointsToAnalysis pta = PointsToAnalysis.fromSolver(solver);

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    AllocationNode newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);

    assertEquals(
        Collections.singleton(newField),
        pta.reachingObjects(SparkTestUtil.var(fieldType, "$stack5", mainSig)));
    assertEquals(
        Collections.singleton(newField),
        pta.reachingObjects(SparkTestUtil.var(fieldType, "l2", mainSig)));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(SparkTestUtil.var(containerType, "$stack6", mainSig)));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(SparkTestUtil.var(containerType, "l3", mainSig)));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(SparkTestUtil.var(containerType, "l4", mainSig)));

    assertEquals(
        Collections.singleton((Type) fieldType),
        pta.reachingTypes(SparkTestUtil.var(fieldType, "l2", mainSig)));
    assertEquals(
        Collections.singleton((Type) containerType),
        pta.reachingTypes(SparkTestUtil.var(containerType, "l3", mainSig)));
  }

  @Test
  public void aliasesBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Solver solver = SparkTestUtil.solveMainWithSolver(mainSig);
    PointsToAnalysis pta = PointsToAnalysis.fromSolver(solver);

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    Node stack5 = SparkTestUtil.var(fieldType, "$stack5", mainSig);
    Node l2 = SparkTestUtil.var(fieldType, "l2", mainSig);
    Node stack6 = SparkTestUtil.var(containerType, "$stack6", mainSig);
    Node l3 = SparkTestUtil.var(containerType, "l3", mainSig);
    Node l4 = SparkTestUtil.var(containerType, "l4", mainSig);

    Set<Node> aliasesOfL2 = pta.aliases(l2);
    assertTrue(aliasesOfL2.contains(stack5), "l2 and $stack5 share alloc(Field,1)");
    assertFalse(aliasesOfL2.contains(l3), "Field local must not alias Container local");
    assertFalse(aliasesOfL2.contains(l2), "aliases() must exclude the query node itself");

    Set<Node> aliasesOfL3 = pta.aliases(l3);
    assertTrue(aliasesOfL3.contains(l4), "l3 and l4 share alloc(Container,2)");
    assertTrue(aliasesOfL3.contains(stack6), "l3 and $stack6 share alloc(Container,2)");
    assertFalse(aliasesOfL3.contains(l2));
  }

  @Test
  public void instanceFieldRefReachingObjects() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Solver solver = SparkTestUtil.solveMainWithSolver(mainSig);
    PointsToAnalysis pta = PointsToAnalysis.fromSolver(solver);

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    // Build the IFR node for $stack6.field — same shape as the one inserted by the visitor in
    // testMethodToPAGBasic. Its reaching objects should be alloc(Field, 1) via heap propagation.
    InstanceFieldRefNode stack6FieldRef =
        SparkTestUtil.fieldRef(
            SparkTestUtil.var(containerType, "$stack6", mainSig),
            SparkTestUtil.idFactory.getFieldSignature("field", containerType, fieldType),
            fieldType,
            mainSig);

    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    assertEquals(Collections.singleton(newField), pta.reachingObjects(stack6FieldRef));
  }

  @Test
  public void interproceduralLoadAndReturn() {
    // BasicInter: t = bar(q); inside bar, returns s.f. After main: p.f = r, so heap[alloc(O,1), f]
    // contains alloc(O,2). bar's parameter s aliases p, so s.f loads alloc(O,2) → returned to t.
    ClassType classSig = SparkTestUtil.idFactory.getClassType("BasicInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Solver solver = SparkTestUtil.solveMainWithSolver(mainSig);
    PointsToAnalysis pta = PointsToAnalysis.fromSolver(solver);

    ClassType oType = SparkTestUtil.idFactory.getClassType("BasicInter$O");
    AllocationNode newO1 = SparkTestUtil.alloc(oType, 1L, mainSig);
    AllocationNode newO2 = SparkTestUtil.alloc(oType, 2L, mainSig);

    Node p = SparkTestUtil.var(oType, "l1", mainSig);
    Node q = SparkTestUtil.var(oType, "l2", mainSig);
    Node r = SparkTestUtil.var(oType, "l3", mainSig);
    Node t = SparkTestUtil.var(oType, "l4", mainSig);

    assertEquals(Collections.singleton(newO1), pta.reachingObjects(p));
    assertEquals(Collections.singleton(newO1), pta.reachingObjects(q));
    assertEquals(Collections.singleton(newO2), pta.reachingObjects(r));
    // t = bar(q).f: bar returns s.f where s is aliased with p, so s.f contains alloc(O,2).
    assertEquals(Collections.singleton(newO2), pta.reachingObjects(t));

    assertTrue(pta.aliases(p).contains(q), "p and q must alias (q = p)");
    assertTrue(
        pta.aliases(t).contains(r), "t and r must alias (t holds the value of r via the field)");
  }
}
