package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.spark.Engine;
import sootup.spark.PointsToAnalysis;
import sootup.spark.Spark;
import sootup.spark.node.AllocationNode;
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
    Spark spark = SparkTestUtil.solveMainWithSpark(mainSig);
    PointsToAnalysis pta = spark.getPointsToAnalysis();

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    AllocationNode newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);

    assertEquals(
        Collections.singleton(newField),
        pta.reachingObjects(new Local("$stack5", fieldType), mainSig));
    assertEquals(
        Collections.singleton(newField), pta.reachingObjects(new Local("l2", fieldType), mainSig));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(new Local("$stack6", containerType), mainSig));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(new Local("l3", containerType), mainSig));
    assertEquals(
        Collections.singleton(newContainer),
        pta.reachingObjects(new Local("l4", containerType), mainSig));

    assertEquals(
        Collections.singleton((Type) fieldType),
        pta.reachingTypes(new Local("l2", fieldType), mainSig));
    assertEquals(
        Collections.singleton((Type) containerType),
        pta.reachingTypes(new Local("l3", containerType), mainSig));
  }

  @Test
  public void aliasesBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Spark spark = SparkTestUtil.solveMainWithSpark(mainSig);
    PointsToAnalysis pta = spark.getPointsToAnalysis();

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    Local l2 = new Local("l2", fieldType);
    Local l3 = new Local("l3", containerType);

    Node stack5Node = SparkTestUtil.var(fieldType, "$stack5", mainSig);
    Node l2Node = SparkTestUtil.var(fieldType, "l2", mainSig);
    Node stack6Node = SparkTestUtil.var(containerType, "$stack6", mainSig);
    Node l3Node = SparkTestUtil.var(containerType, "l3", mainSig);
    Node l4Node = SparkTestUtil.var(containerType, "l4", mainSig);

    Set<Node> aliasesOfL2 = pta.aliases(l2, mainSig);
    assertTrue(aliasesOfL2.contains(stack5Node), "l2 and $stack5 share alloc(Field,1)");
    assertFalse(aliasesOfL2.contains(l3Node), "Field local must not alias Container local");
    assertFalse(aliasesOfL2.contains(l2Node), "aliases() must exclude the query node itself");

    Set<Node> aliasesOfL3 = pta.aliases(l3, mainSig);
    assertTrue(aliasesOfL3.contains(l4Node), "l3 and l4 share alloc(Container,2)");
    assertTrue(aliasesOfL3.contains(stack6Node), "l3 and $stack6 share alloc(Container,2)");
    assertFalse(aliasesOfL3.contains(l2Node));
  }

  @Test
  public void instanceFieldRefReachingObjects() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Spark spark = SparkTestUtil.solveMainWithSpark(mainSig);
    PointsToAnalysis pta = spark.getPointsToAnalysis();

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    // $stack6.field — same shape as the IFR inserted by the visitor in testMethodToPAGBasic.
    // Its reaching objects should be alloc(Field, 1) via heap propagation.
    FieldSignature fieldSig =
        SparkTestUtil.idFactory.getFieldSignature("field", containerType, fieldType);
    JInstanceFieldRef stack6FieldRef =
        new JInstanceFieldRef(new Local("$stack6", containerType), fieldSig);

    AllocationNode newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    assertEquals(Collections.singleton(newField), pta.reachingObjects(stack6FieldRef, mainSig));
  }

  @Test
  public void interproceduralLoadAndReturn() {
    // BasicInter: t = bar(q); inside bar, returns s.f. After main: p.f = r, so heap[alloc(O,1), f]
    // contains alloc(O,2). bar's parameter s aliases p, so s.f loads alloc(O,2) → returned to t.
    ClassType classSig = SparkTestUtil.idFactory.getClassType("BasicInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Spark spark = SparkTestUtil.solveMainWithSpark(mainSig);
    PointsToAnalysis pta = spark.getPointsToAnalysis();

    ClassType oType = SparkTestUtil.idFactory.getClassType("BasicInter$O");
    AllocationNode newO1 = SparkTestUtil.alloc(oType, 1L, mainSig);
    AllocationNode newO2 = SparkTestUtil.alloc(oType, 2L, mainSig);

    Local p = new Local("l1", oType);
    Local r = new Local("l3", oType);
    Local t = new Local("l4", oType);

    Node rNode = SparkTestUtil.var(oType, "l3", mainSig);

    assertEquals(Collections.singleton(newO1), pta.reachingObjects(p, mainSig));
    assertEquals(Collections.singleton(newO2), pta.reachingObjects(r, mainSig));
    // t = bar(q).f: bar returns s.f where s is aliased with p, so s.f contains alloc(O,2).
    assertEquals(Collections.singleton(newO2), pta.reachingObjects(t, mainSig));

    assertTrue(
        pta.aliases(t, mainSig).contains(rNode),
        "t and r must alias (t holds the value of r via the field)");
  }

  /**
   * Callee parameter nodes must receive points-to sets via the inter-procedural edges produced by
   * the {@code caseInvokeStmt} fix. Before the fix, {@code copyValue}'s formals had empty pts.
   */
  @Test
  public void voidCallInterParamReachingObjects() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("VoidCallInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    ClassType oType = SparkTestUtil.idFactory.getClassType("VoidCallInter$O");
    MethodSignature copyValueSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, "copyValue", VoidType.getInstance(), Arrays.asList(oType, oType));

    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();
    AllocationNode newO1 = SparkTestUtil.alloc(oType, 1L, mainSig);
    AllocationNode newO3 = SparkTestUtil.alloc(oType, 3L, mainSig);

    // dst(l0) receives t (alloc3); src(l1) receives q which aliases p (alloc1)
    assertEquals(
        Collections.singleton(newO3), pta.reachingObjects(new Local("l0", oType), copyValueSig));
    assertEquals(
        Collections.singleton(newO1), pta.reachingObjects(new Local("l1", oType), copyValueSig));
  }

  /**
   * Caller args and their matching callee params must alias across the call boundary — uniquely
   * exercised by the {@code JInvokeStmt} (void-call) param-passing edges.
   */
  @Test
  public void voidCallInterCrossMethodAliases() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("VoidCallInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    ClassType oType = SparkTestUtil.idFactory.getClassType("VoidCallInter$O");
    MethodSignature copyValueSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, "copyValue", VoidType.getInstance(), Arrays.asList(oType, oType));

    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();
    Node dstNode = SparkTestUtil.var(oType, "l0", copyValueSig);
    Node srcNode = SparkTestUtil.var(oType, "l1", copyValueSig);

    // q (passed as src) aliases copyValue:src; t (passed as dst) aliases copyValue:dst
    assertTrue(
        pta.aliases(new Local("l2", oType), mainSig).contains(srcNode),
        "q aliases copyValue:src across call boundary");
    assertTrue(
        pta.aliases(new Local("l4", oType), mainSig).contains(dstNode),
        "t aliases copyValue:dst across call boundary");
  }

  /**
   * {@code t.f} must reach alloc2 (r) after {@code copyValue(t, q)}: inside copyValue {@code dst.f
   * = src.f} propagates {@code heap[alloc1,f]={alloc2}} into {@code heap[alloc3,f]} — an
   * inter-procedural heap mutation invisible without the fix.
   */
  @Test
  public void voidCallInterHeapPropagationAfterCopy() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("VoidCallInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    ClassType oType = SparkTestUtil.idFactory.getClassType("VoidCallInter$O");

    PointsToAnalysis pta = SparkTestUtil.solveMainWithSpark(mainSig).getPointsToAnalysis();
    AllocationNode newO2 = SparkTestUtil.alloc(oType, 2L, mainSig);
    FieldSignature fSig = SparkTestUtil.idFactory.getFieldSignature("f", oType, oType);

    // t.f must reach alloc2 via the inter-proc store chain: dst.f = src.f where src aliases p
    assertEquals(
        Collections.singleton(newO2),
        pta.reachingObjects(new JInstanceFieldRef(new Local("l4", oType), fSig), mainSig));
  }
}
