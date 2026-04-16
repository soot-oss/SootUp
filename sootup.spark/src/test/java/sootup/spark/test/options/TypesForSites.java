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
import sootup.spark.node.VariableNode;

public class TypesForSites {

  @BeforeEach
  public void reset() {
    Engine.resetAllocCount();
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

    SparkOptions options = SparkOptions.builder().typesForSites(true).build();
    Solver solver =
        Solver.builder()
            .view(view)
            .entryPoints(Collections.singletonList(mainMethodSig))
            .sparkOptions(options)
            .build();
    solver.solve();

    PAG pag = solver.getPag();
    var delegate = pag.getDelegate();

    ClassType oType = idFactory.getClassType("MultiAllocSameType$O");

    // Single allocation node for all allocations of the same type (no allocationSite)
    var newO = AllocationNode.builder().type(oType).containingMethodSig(mainMethodSig).build();

    var stack4 =
        VariableNode.builder()
            .type(oType)
            .name("$stack4")
            .containingMethodSig(mainMethodSig)
            .build();
    var stack5 =
        VariableNode.builder()
            .type(oType)
            .name("$stack5")
            .containingMethodSig(mainMethodSig)
            .build();
    var stack6 =
        VariableNode.builder()
            .type(oType)
            .name("$stack6")
            .containingMethodSig(mainMethodSig)
            .build();
    var l1 =
        VariableNode.builder().type(oType).name("l1").containingMethodSig(mainMethodSig).build();
    var l2 =
        VariableNode.builder().type(oType).name("l2").containingMethodSig(mainMethodSig).build();
    var l3 =
        VariableNode.builder().type(oType).name("l3").containingMethodSig(mainMethodSig).build();

    // All three stack variables point to the single allocation node
    assertTrue(delegate.containsEdge(newO, stack4), "Edge new O -> $stack4 not found");
    assertTrue(delegate.containsEdge(newO, stack5), "Edge new O -> $stack5 not found");
    assertTrue(delegate.containsEdge(newO, stack6), "Edge new O -> $stack6 not found");

    assertTrue(delegate.containsEdge(stack4, l1), "Edge $stack4 -> l1 not found");
    assertTrue(delegate.containsEdge(stack5, l2), "Edge $stack5 -> l2 not found");
    assertTrue(delegate.containsEdge(stack6, l3), "Edge $stack6 -> l3 not found");
  }
}
