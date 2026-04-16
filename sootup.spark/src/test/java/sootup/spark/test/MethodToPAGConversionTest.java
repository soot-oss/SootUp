package sootup.spark.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;
import sootup.spark.Engine;
import sootup.spark.PAG;
import sootup.spark.Solver;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.VariableNode;

public class MethodToPAGConversionTest {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
  }

  @Test
  public void testMethodToPAGBasic() {
    AnalysisInputLocation input =
        new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary");
    View view = new JavaView(input);
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    ClassType classSig = idFactory.getClassType("Basic");
    Optional<? extends SootClass> classOpt = view.getClass(classSig);
    assertTrue(classOpt.isPresent());

    MethodSignature mainMethodSig =
        idFactory.getMethodSignature(classSig, idFactory.getMainSubSignature());

    Optional<? extends SootMethod> method = view.getMethod(mainMethodSig);
    assertTrue(method.isPresent());

    Solver solver =
        Solver.builder().view(view).entryPoints(Collections.singletonList(mainMethodSig)).build();
    solver.solve();

    PAG pag = solver.getPag();

    var delegate = pag.getDelegate();

    ClassType fieldType = idFactory.getClassType("Basic$Field");
    ClassType containerType = idFactory.getClassType("Basic$Container");

    var newField =
        AllocationNode.builder()
            .type(fieldType)
            .allocationSite(1L)
            .containingMethodSig(mainMethodSig)
            .build();
    var fieldStack5 =
        VariableNode.builder()
            .type(fieldType)
            .name("$stack5")
            .containingMethodSig(mainMethodSig)
            .build();
    var newContainer =
        AllocationNode.builder()
            .type(containerType)
            .allocationSite(2L)
            .containingMethodSig(mainMethodSig)
            .build();
    var containerStack6 =
        VariableNode.builder()
            .type(containerType)
            .name("$stack6")
            .containingMethodSig(mainMethodSig)
            .build();
    var fieldL2 =
        VariableNode.builder()
            .type(fieldType)
            .name("l2")
            .containingMethodSig(mainMethodSig)
            .build();
    var containerL3 =
        VariableNode.builder()
            .type(containerType)
            .name("l3")
            .containingMethodSig(mainMethodSig)
            .build();
    var containerL4 =
        VariableNode.builder()
            .type(containerType)
            .name("l4")
            .containingMethodSig(mainMethodSig)
            .build();
    var fieldRef =
        InstanceFieldRefNode.builder()
            .base(containerStack6)
            .field(idFactory.getFieldSignature("field", containerType, fieldType))
            .type(fieldType)
            .containingMethodSig(mainMethodSig)
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
    AnalysisInputLocation input =
        new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary");
    View view = new JavaView(input);
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    ClassType classSig = idFactory.getClassType("MultiAllocSameType");
    Optional<? extends SootClass> classOpt = view.getClass(classSig);
    assertTrue(classOpt.isPresent());

    MethodSignature mainMethodSig =
        idFactory.getMethodSignature(classSig, idFactory.getMainSubSignature());

    Optional<? extends SootMethod> method = view.getMethod(mainMethodSig);
    assertTrue(method.isPresent());

    Solver solver =
        Solver.builder().view(view).entryPoints(Collections.singletonList(mainMethodSig)).build();
    solver.solve();

    PAG pag = solver.getPag();

    var delegate = pag.getDelegate();

    ClassType oType = idFactory.getClassType("MultiAllocSameType$O");

    // First allocation
    var newO1 =
        AllocationNode.builder()
            .type(oType)
            .allocationSite(1L)
            .containingMethodSig(mainMethodSig)
            .build();
    var stack4 =
        VariableNode.builder()
            .type(oType)
            .name("$stack4")
            .containingMethodSig(mainMethodSig)
            .build();
    var l1 =
        VariableNode.builder().type(oType).name("l1").containingMethodSig(mainMethodSig).build();

    // Second allocation
    var newO2 =
        AllocationNode.builder()
            .type(oType)
            .allocationSite(2L)
            .containingMethodSig(mainMethodSig)
            .build();
    var stack5 =
        VariableNode.builder()
            .type(oType)
            .name("$stack5")
            .containingMethodSig(mainMethodSig)
            .build();
    var l2 =
        VariableNode.builder().type(oType).name("l2").containingMethodSig(mainMethodSig).build();

    // Third allocation
    var newO3 =
        AllocationNode.builder()
            .type(oType)
            .allocationSite(3L)
            .containingMethodSig(mainMethodSig)
            .build();
    var stack6 =
        VariableNode.builder()
            .type(oType)
            .name("$stack6")
            .containingMethodSig(mainMethodSig)
            .build();
    var l3 =
        VariableNode.builder().type(oType).name("l3").containingMethodSig(mainMethodSig).build();

    // Test all edges exist
    assertTrue(delegate.containsEdge(newO1, stack4), "Edge new O (1) -> $stack4 not found");
    assertTrue(delegate.containsEdge(stack4, l1), "Edge $stack4 -> l1 not found");

    assertTrue(delegate.containsEdge(newO2, stack5), "Edge new O (2) -> $stack5 not found");
    assertTrue(delegate.containsEdge(stack5, l2), "Edge $stack5 -> l2 not found");

    assertTrue(delegate.containsEdge(newO3, stack6), "Edge new O (3) -> $stack6 not found");
    assertTrue(delegate.containsEdge(stack6, l3), "Edge $stack6 -> l3 not found");
  }

  @Test
  public void testMethodToPAGBasicInter() {
    AnalysisInputLocation input =
        new JavaClassPathAnalysisInputLocation("src/test/resources/pta/binary");
    View view = new JavaView(input);
    JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    ClassType classSig = idFactory.getClassType("BasicInter");
    Optional<? extends SootClass> classOpt = view.getClass(classSig);
    assertTrue(classOpt.isPresent());

    MethodSignature mainMethodSig =
        idFactory.getMethodSignature(classSig, idFactory.getMainSubSignature());

    Optional<? extends SootMethod> method = view.getMethod(mainMethodSig);
    assertTrue(method.isPresent());

    Solver solver =
        Solver.builder().view(view).entryPoints(Collections.singletonList(mainMethodSig)).build();
    solver.solve();

    PAG pag = solver.getPag();
    var delegate = pag.getDelegate();

    ClassType oType = idFactory.getClassType("BasicInter$O");
    MethodSignature barMethodSig =
        idFactory.getMethodSignature(
            classSig, "bar", "BasicInter$O", Collections.singletonList("BasicInter$O"));

    // main nodes
    var newO1 =
        AllocationNode.builder()
            .type(oType)
            .allocationSite(1L)
            .containingMethodSig(mainMethodSig)
            .build();
    var stack5 =
        VariableNode.builder()
            .type(oType)
            .name("$stack5")
            .containingMethodSig(mainMethodSig)
            .build();
    var l1 =
        VariableNode.builder().type(oType).name("l1").containingMethodSig(mainMethodSig).build();
    var l2 =
        VariableNode.builder().type(oType).name("l2").containingMethodSig(mainMethodSig).build();
    var newO2 =
        AllocationNode.builder()
            .type(oType)
            .allocationSite(2L)
            .containingMethodSig(mainMethodSig)
            .build();
    var stack6 =
        VariableNode.builder()
            .type(oType)
            .name("$stack6")
            .containingMethodSig(mainMethodSig)
            .build();
    var l3 =
        VariableNode.builder().type(oType).name("l3").containingMethodSig(mainMethodSig).build();
    var stack5fRef =
        InstanceFieldRefNode.builder()
            .base(stack5)
            .field(idFactory.getFieldSignature("f", oType, oType))
            .type(oType)
            .containingMethodSig(mainMethodSig)
            .build();

    // bar nodes
    var l0 =
        VariableNode.builder().type(oType).name("l0").containingMethodSig(barMethodSig).build();
    var l0fRef =
        InstanceFieldRefNode.builder()
            .base(l0)
            .field(idFactory.getFieldSignature("f", oType, oType))
            .type(oType)
            .containingMethodSig(barMethodSig)
            .build();
    var stack1 =
        VariableNode.builder()
            .type(oType)
            .name("$stack1")
            .containingMethodSig(barMethodSig)
            .build();
    var l4 =
        VariableNode.builder().type(oType).name("l4").containingMethodSig(mainMethodSig).build();

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
