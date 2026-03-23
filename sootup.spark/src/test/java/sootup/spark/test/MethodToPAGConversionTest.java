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

    var newField = AllocationNode.builder().type(fieldType).allocationSite(1L).build();
    var fieldStack5 = VariableNode.builder().type(fieldType).name("$stack5").build();
    var newContainer = AllocationNode.builder().type(containerType).allocationSite(2L).build();
    var containerStack6 = VariableNode.builder().type(containerType).name("$stack6").build();
    var fieldL2 = VariableNode.builder().type(fieldType).name("l2").build();
    var containerL3 = VariableNode.builder().type(containerType).name("l3").build();
    var containerL4 = VariableNode.builder().type(containerType).name("l4").build();
    var fieldRef =
        InstanceFieldRefNode.builder()
            .base(containerStack6)
            .field(idFactory.getFieldSignature("field", containerType, fieldType))
            .type(fieldType)
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
    var newO1 = AllocationNode.builder().type(oType).allocationSite(1L).build();
    var stack4 = VariableNode.builder().type(oType).name("$stack4").build();
    var l1 = VariableNode.builder().type(oType).name("l1").build();

    // Second allocation
    var newO2 = AllocationNode.builder().type(oType).allocationSite(2L).build();
    var stack5 = VariableNode.builder().type(oType).name("$stack5").build();
    var l2 = VariableNode.builder().type(oType).name("l2").build();

    // Third allocation
    var newO3 = AllocationNode.builder().type(oType).allocationSite(3L).build();
    var stack6 = VariableNode.builder().type(oType).name("$stack6").build();
    var l3 = VariableNode.builder().type(oType).name("l3").build();

    // Test all edges exist
    assertTrue(delegate.containsEdge(newO1, stack4), "Edge new O (1) -> $stack4 not found");
    assertTrue(delegate.containsEdge(stack4, l1), "Edge $stack4 -> l1 not found");

    assertTrue(delegate.containsEdge(newO2, stack5), "Edge new O (2) -> $stack5 not found");
    assertTrue(delegate.containsEdge(stack5, l2), "Edge $stack5 -> l2 not found");

    assertTrue(delegate.containsEdge(newO3, stack6), "Edge new O (3) -> $stack6 not found");
    assertTrue(delegate.containsEdge(stack6, l3), "Edge $stack6 -> l3 not found");
  }
}
