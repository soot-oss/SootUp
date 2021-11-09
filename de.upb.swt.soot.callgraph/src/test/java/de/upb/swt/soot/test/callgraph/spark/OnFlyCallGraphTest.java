package de.upb.swt.soot.test.callgraph.spark;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import de.upb.swt.soot.callgraph.algorithm.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.algorithm.ClassHierarchyAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.spark.Spark;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

public class OnFlyCallGraphTest extends SparkTestBase {

  protected String testDirectory, className;
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  private static Map<String, JavaView> viewToClassPath = new HashMap<>();

  private JavaView createViewForClassPath(String classPath) {
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    return javaProject.createOnDemandView();
  }

  protected CallGraph loadCallGraph(String testDirectory, String className) {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath = "src/test/resources/callgraph/" + testDirectory;

    JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).get();
    Optional<SootMethod> m =
        (Optional<SootMethod>) sc.getMethod(mainMethodSignature.getSubSignature());
    assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

    Spark spark = new Spark.Builder(view, null, Collections.singletonList(mainMethodSignature)).onFlyCFG(true).build();
    spark.analyze();
    m.get().getBody().getLocals().forEach(local -> System.out.println("Local " + local + " -> " + spark.getPointsToSet(local)));
    System.out.println(m.get().getBody().getStmts());

    CallGraph cg = spark.getCallGraph();

    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);
    return cg;
  }
  @Test
  public void testSingleMethod() {
    CallGraph cg = loadCallGraph("Misc", "example.SingleMethod");
    assertEquals(0, cg.callCount());
    assertEquals(0, cg.callsTo(mainMethodSignature).size());
    assertEquals(0, cg.callsFrom(mainMethodSignature).size());
  }

  @Test
  public void testAddClass() {

  }

  @Test
  public void testRecursiveCall() {
    CallGraph cg = loadCallGraph("Misc", "recur.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.emptyList());

    MethodSignature uncalledMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.singletonList("int"));

    assertTrue(cg.containsMethod(mainMethodSignature));
    assertTrue(cg.containsMethod(method));
    assertFalse(cg.containsMethod(uncalledMethod));
    TestCase.assertEquals(2, cg.getMethodSignatures().size());

    assertTrue(cg.containsCall(mainMethodSignature, mainMethodSignature));
    assertTrue(cg.containsCall(mainMethodSignature, method));
    TestCase.assertEquals(2, cg.callsFrom(mainMethodSignature).size());
  }

  @Test
  public void testNonVirtualCall1() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc1.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testNonVirtualCall2() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc2.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "<init>", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testNonVirtualCall3() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc3.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.emptyList());
    MethodSignature uncalledMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.singletonList("int"));
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
    assertFalse(cg.containsMethod(uncalledMethod));
  }

  @Test
  public void testNonVirtualCall4() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc4.Class");
    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, firstMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("nvc4.Rootclass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(firstMethod, targetMethod));
  }

  @Test
  public void testNonVirtualCall5() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc5.Demo");

    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            "method", identifierFactory.getClassType("nvc5.Sub"), "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, firstMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("nvc5.Middle"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(firstMethod, targetMethod));
  }

  @Test
  public void testVirtualCall1() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc1.Class");

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "target", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testVirtualCall2() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc2.Class");

    JavaClassType subClassSig = identifierFactory.getClassType("vc2.SubClass");
    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            "<init>", subClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "callMethod", mainClassSignature, "void", Collections.singletonList("vc2.Class"));
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", identifierFactory.getClassType("vc2.Class"), "void", Collections.emptyList());
    assertTrue(cg.containsCall(callMethod, targetMethod));
  }

  @Test
  public void testVirtualCall3() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc3.Class");

    JavaClassType subClassSig = identifierFactory.getClassType("vc3.ClassImpl");
    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            "<init>", subClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "callOnInterface",
            mainClassSignature,
            "void",
            Collections.singletonList("vc3.Interface"));
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", subClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(callMethod, targetMethod));
  }

  @Test
  public void testVirtualCall4() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc4.Class");

    // more precise its: declareClassSig
    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("vc4.Interface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod1() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim1.Class");
    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim1.Interface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod2() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim2.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim2.Interface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod3() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim3.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod4() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim4.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim4.Interface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod5() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim5.SuperClass");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim5.DirectInterface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, method));

    MethodSignature compute =
        identifierFactory.getMethodSignature(
            "compute", mainClassSignature, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, compute));
  }

  @Ignore
  // TODO: WALA can't handle this case?
  public void testDynamicInterfaceMethod6() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim6.Demo");

    MethodSignature combinedInterfaceMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim6.CombinedInterface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, combinedInterfaceMethod));

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim6.SomeInterface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(combinedInterfaceMethod, method));

    MethodSignature anotherMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim6.AnotherInterface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(combinedInterfaceMethod, anotherMethod));
  }

  @Test
  public void testStaticInterfaceMethod() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8sim.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8sim.Interface"),
            "void",
            Collections.emptyList());

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }

  @Ignore
  public void testHelloWorld() {
    CallGraph cg = loadCallGraph("Misc", "HelloWorld");

    ClassType clazzType = JavaIdentifierFactory.getInstance().getClassType("java.io.PrintStream");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "println", clazzType, "void", Collections.singletonList("java.lang.String"));

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }
}
