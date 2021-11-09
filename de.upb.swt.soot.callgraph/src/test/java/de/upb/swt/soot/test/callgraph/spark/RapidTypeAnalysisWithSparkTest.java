package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.RapidTypeAnalysisWithSpark;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.swt.soot.test.callgraph.CallGraphTestBase;
import java.util.Collections;
import java.util.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kadiray Karakaya */
@Category(Java8Test.class)
public class RapidTypeAnalysisWithSparkTest extends CallGraphTestBase<RapidTypeAnalysisWithSpark> {

  JavaView view;
  TypeHierarchy typeHierarchy;

  @Override
  protected RapidTypeAnalysisWithSpark createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    this.view = view;
    this.typeHierarchy = typeHierarchy;
    return null;
  }

  @Override
  protected CallGraph loadCallGraph(String testDirectory, String className) {
    String pointerBenchClassPath = "src/test/resources/callgraph/" + testDirectory;

    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(pointerBenchClassPath))
            .build();

    JavaView view = javaProject.createOnDemandView();

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    SootClass sc = (SootClass) view.getClass(mainClassSignature).get();
    Optional<SootMethod> m = sc.getMethod(mainMethodSignature.getSubSignature());
    assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    createAlgorithm(view, typeHierarchy);

    CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph callGraph = algorithm.initialize(Collections.singletonList(mainMethodSignature));
    Spark spark = new Spark.Builder(view, callGraph, Collections.emptyList()).rta(true).build();
    spark.analyze();
    return spark.getCallGraph();
  }

  @Test
  public void testMiscExample1() {
    /** We expect constructors for B, C and E We expect A.print(), B.print(), C.print() */
    CallGraph cg = loadCallGraph("Misc", "example1.Example");

    // TODO also construct ofcg and test if they are equal

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.emptyList());

    MethodSignature constructorE =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.E"),
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.A"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.D"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodE =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.E"),
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));
    assertTrue(cg.containsCall(mainMethodSignature, constructorE));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertTrue(cg.containsCall(mainMethodSignature, methodE));
    assertFalse(cg.containsMethod(methodD));

    assertEquals(7, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(constructorE).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());
    assertEquals(1, cg.callsTo(methodE).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
    assertEquals(0, cg.callsFrom(methodE).size());
  }

  @Ignore
  public void testAddClass() {
    // Spark currently does not expose the call graph algorithm
  }
}
