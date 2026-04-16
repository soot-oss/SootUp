package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.jgrapht.Graph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.spark.Engine;
import sootup.spark.PAGEdge;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.Node;

public class MethodToPAGConversionTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void testMethodToPAGBasic() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("Basic");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig);

    ClassType fieldType = SparkTestUtil.idFactory.getClassType("Basic$Field");
    ClassType containerType = SparkTestUtil.idFactory.getClassType("Basic$Container");

    var newField = SparkTestUtil.alloc(fieldType, 1L, mainSig);
    var fieldStack5 = SparkTestUtil.var(fieldType, "$stack5", mainSig);
    var newContainer = SparkTestUtil.alloc(containerType, 2L, mainSig);
    var containerStack6 = SparkTestUtil.var(containerType, "$stack6", mainSig);
    var fieldL2 = SparkTestUtil.var(fieldType, "l2", mainSig);
    var containerL3 = SparkTestUtil.var(containerType, "l3", mainSig);
    var containerL4 = SparkTestUtil.var(containerType, "l4", mainSig);
    var fieldRef =
        InstanceFieldRefNode.builder()
            .base(containerStack6)
            .field(SparkTestUtil.idFactory.getFieldSignature("field", containerType, fieldType))
            .type(fieldType)
            .containingMethodSig(mainSig)
            .build();

    assertTrue(delegate.containsEdge(newField, fieldStack5), "Edge new Field -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(newContainer, containerStack6),
        "Edge new Container -> $stack6 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldL2), "Edge $stack5 -> l2 not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL3), "Edge $stack6 -> l3 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldRef), "Edge $stack5 -> field ref not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL4), "Edge $stack6 -> l4 not found");
  }

  @Test
  public void testMethodToPAGMultiAllocSameType() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("MultiAllocSameType");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig);

    ClassType oType = SparkTestUtil.idFactory.getClassType("MultiAllocSameType$O");

    // First allocation
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(oType, 1L, mainSig), SparkTestUtil.var(oType, "$stack4", mainSig)),
        "Edge new O (1) -> $stack4 not found");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack4", mainSig), SparkTestUtil.var(oType, "l1", mainSig)),
        "Edge $stack4 -> l1 not found");

    // Second allocation
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(oType, 2L, mainSig), SparkTestUtil.var(oType, "$stack5", mainSig)),
        "Edge new O (2) -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack5", mainSig), SparkTestUtil.var(oType, "l2", mainSig)),
        "Edge $stack5 -> l2 not found");

    // Third allocation
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(oType, 3L, mainSig), SparkTestUtil.var(oType, "$stack6", mainSig)),
        "Edge new O (3) -> $stack6 not found");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(oType, "$stack6", mainSig), SparkTestUtil.var(oType, "l3", mainSig)),
        "Edge $stack6 -> l3 not found");
  }

  @Test
  public void testMethodToPAGStringAlloc() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("StringAlloc");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig);

    ClassType stringType = SparkTestUtil.idFactory.getClassType("java.lang.String");

    // String s1 = "s1" — string constant definition: allocation node 1 → local l1
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(stringType, 1L, mainSig),
            SparkTestUtil.var(stringType, "l1", mainSig)),
        "alloc(String,1) -> l1");
    // String s2 = "s2" — string constant definition: allocation node 2 → local l2
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(stringType, 2L, mainSig),
            SparkTestUtil.var(stringType, "l2", mainSig)),
        "alloc(String,2) -> l2");
    // String s3 = new String("s3") — explicit new: allocation node 3 → temp $stack4 → local l3
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.alloc(stringType, 3L, mainSig),
            SparkTestUtil.var(stringType, "$stack4", mainSig)),
        "alloc(String,3) -> $stack4");
    assertTrue(
        delegate.containsEdge(
            SparkTestUtil.var(stringType, "$stack4", mainSig),
            SparkTestUtil.var(stringType, "l3", mainSig)),
        "$stack4 -> l3");
  }

  @Test
  public void testMethodToPAGBasicInter() {
    ClassType classSig = SparkTestUtil.idFactory.getClassType("BasicInter");
    MethodSignature mainSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, SparkTestUtil.idFactory.getMainSubSignature());
    MethodSignature barSig =
        SparkTestUtil.idFactory.getMethodSignature(
            classSig, "bar", "BasicInter$O", Collections.singletonList("BasicInter$O"));
    Graph<Node, PAGEdge> delegate = SparkTestUtil.solveMain(mainSig);

    ClassType oType = SparkTestUtil.idFactory.getClassType("BasicInter$O");

    // main nodes
    var newO1 = SparkTestUtil.alloc(oType, 1L, mainSig);
    var stack5 = SparkTestUtil.var(oType, "$stack5", mainSig);
    var l1 = SparkTestUtil.var(oType, "l1", mainSig);
    var l2 = SparkTestUtil.var(oType, "l2", mainSig);
    var newO2 = SparkTestUtil.alloc(oType, 2L, mainSig);
    var stack6 = SparkTestUtil.var(oType, "$stack6", mainSig);
    var l3 = SparkTestUtil.var(oType, "l3", mainSig);
    var l4 = SparkTestUtil.var(oType, "l4", mainSig);
    var stack5fRef =
        InstanceFieldRefNode.builder()
            .base(stack5)
            .field(SparkTestUtil.idFactory.getFieldSignature("f", oType, oType))
            .type(oType)
            .containingMethodSig(mainSig)
            .build();

    // bar nodes
    var l0 = SparkTestUtil.var(oType, "l0", barSig);
    var stack1 = SparkTestUtil.var(oType, "$stack1", barSig);
    var l0fRef =
        InstanceFieldRefNode.builder()
            .base(l0)
            .field(SparkTestUtil.idFactory.getFieldSignature("f", oType, oType))
            .type(oType)
            .containingMethodSig(barSig)
            .build();

    // main intraprocedural edges
    assertTrue(delegate.containsEdge(newO1, stack5), "alloc(O,1) -> $stack5");
    assertTrue(delegate.containsEdge(stack5, l1), "$stack5 -> l1");
    assertTrue(delegate.containsEdge(stack5, l2), "$stack5 -> l2");
    assertTrue(delegate.containsEdge(newO2, stack6), "alloc(O,2) -> $stack6");
    assertTrue(delegate.containsEdge(stack6, l3), "$stack6 -> l3");
    assertTrue(delegate.containsEdge(stack6, stack5fRef), "$stack6 -> $stack5.f (store)");

    // interprocedural edge: $stack5 (main arg) -> l0 (bar param)
    assertTrue(delegate.containsEdge(stack5, l0), "$stack5 -> bar:l0 (interprocedural)");

    // bar intraprocedural edge: l0.f -> $stack1 (load)
    assertTrue(delegate.containsEdge(l0fRef, stack1), "bar: l0.f -> $stack1 (load)");

    // interprocedural return edge: bar:$stack1 -> main:l4
    assertTrue(delegate.containsEdge(stack1, l4), "bar:$stack1 -> main:l4 (return value)");
  }
}
