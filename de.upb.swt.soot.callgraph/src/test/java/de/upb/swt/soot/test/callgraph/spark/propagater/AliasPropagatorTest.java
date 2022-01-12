package de.upb.swt.soot.test.callgraph.spark.propagater;

import static junit.framework.TestCase.fail;

import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.builder.SparkOptions;
import de.upb.swt.soot.callgraph.spark.pag.PointerAssignmentGraph;
import de.upb.swt.soot.callgraph.spark.solver.AliasPropagator;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import org.junit.Test;

public class AliasPropagatorTest {

  @Test
  public void test() {

    PointerAssignmentGraph pag = buildPAG("propagator.AliasProp");
    AliasPropagator propagator = new AliasPropagator(pag);
    propagator.propagate();
    System.out.println("abc");
  }

  private PointerAssignmentGraph buildPAG(String className) {

    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(
                new JavaSourcePathAnalysisInputLocation("src/test/resources/spark/PointerBench"))
            .build();

    JavaView view = javaProject.createOnDemandView();

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    JavaClassType mainClassSignature = identifierFactory.getClassType(className);

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            "test", mainClassSignature, "void", Collections.emptyList());

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph callGraph = algorithm.initialize(Collections.singletonList(methodSignature));
    PointerAssignmentGraph pag = new PointerAssignmentGraph(view, callGraph, new SparkOptions());
    return pag;
  }
}
