package sootup.spark.test.options;

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
import sootup.spark.SparkOptions;
import sootup.spark.node.AllocationNode;
import sootup.spark.node.InstanceFieldRefNode;
import sootup.spark.node.VariableNode;
import sootup.spark.test.SparkTestUtil;

public class SimpleEdgesBidirectionalTest {

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

    SparkOptions options = SparkOptions.builder().simpleEdgesBidirectional(true).build();
    Solver solver =
        Solver.builder()
            .view(view)
            .entryPoints(Collections.singletonList(mainMethodSig))
            .sparkOptions(options)
            .build();
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

    SparkTestUtil.vizualizeMehodPAG(delegate);

    assertTrue(delegate.containsEdge(newField, fieldStack5), "Edge new Field -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(newContainer, containerStack6),
        "Edge new Container -> $stack6 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldL2), "Edge $stack5 -> l2 not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL3), "Edge $stack6 -> l3 not found");
    assertTrue(delegate.containsEdge(fieldStack5, fieldRef), "Edge $stack5 -> field ref not found");
    assertTrue(delegate.containsEdge(containerStack6, containerL4), "Edge $stack6 -> l4 not found");

    // Reverse edges due to simpleEdgesBidirectional
    assertTrue(
        delegate.containsEdge(fieldStack5, newField),
        "Reverse edge $stack5 -> new Field not found");
    assertTrue(
        delegate.containsEdge(containerStack6, newContainer),
        "Reverse edge $stack6 -> new Container not found");
    assertTrue(delegate.containsEdge(fieldL2, fieldStack5), "Reverse edge l2 -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(containerL3, containerStack6),
        "Reverse edge l3 -> $stack6 not found");
    assertTrue(
        delegate.containsEdge(fieldRef, fieldStack5),
        "Reverse edge field ref -> $stack5 not found");
    assertTrue(
        delegate.containsEdge(containerL4, containerStack6),
        "Reverse edge l4 -> $stack6 not found");
  }
}
