package de.upb.swt.soot.test.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.CallGraphAlgorithm;
import de.upb.swt.soot.callgraph.ClassHierarchyAlgorithm;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Input source examples taken from https://bitbucket.org/delors/jcg/src/master/
 *
 * @author: Markus Schmidt
 */
@Category(Java8Test.class)
public class ClassHierarchyAlgorithmTest {

  // TODO: StaticInitializers, Lambdas ?
  JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  JavaClassType mainClassSignature;
  MethodSignature mainMethodSignature;

  CallGraph loadCallGraph(String testDirectory, String className) {
    String walaClassPath = "src/test/resources/callgraph/" + testDirectory;

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addClassPath(new JavaSourcePathAnalysisInputLocation(walaClassPath))
            .build();

    System.out.println(System.getProperty("java.home"));

    View view = javaProject.createOnDemandView();

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", mainClassSignature, "void", Collections.singletonList("java.lang.String[]"));

    SootClass sc = (SootClass) view.getClass(mainClassSignature).get();
    Optional<SootMethod> m = sc.getMethod(mainMethodSignature);
    assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm cha = new ClassHierarchyAlgorithm(view, typeHierarchy);
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    // TODO: remove debuginfo
    System.out.println(cg);

    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);
    return cg;
  }

  @Test
  public void testMiscExample1() {
    /** We expect constructors for B and C We expect A.print(), B.print(), C.print(), D.print() */
    CallGraph cg = loadCallGraph("Misc", "example1.Example");

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

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.A"),
            "void",
            Collections.singletonList("example1.A"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.singletonList("example1.A"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.singletonList("example1.A"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.D"),
            "void",
            Collections.singletonList("example1.A"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertTrue(cg.containsCall(mainMethodSignature, methodD));

    assertEquals(6, cg.callsFrom(mainMethodSignature).size());

    assertEquals(1, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());
    assertEquals(1, cg.callsTo(methodD).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
    assertEquals(0, cg.callsFrom(methodD).size());
  }

  @Test
  public void testAddClass() {

    String walaClassPath = "src/test/resources/callgraph/Misc";

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(new JavaSourcePathAnalysisInputLocation(walaClassPath))
            .build();

    View view = javaProject.createOnDemandView();

    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main",
            identifierFactory.getClassType("update.operation.cg.Class"),
            "void",
            Collections.singletonList("java.lang.String[]"));

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("update.operation.cg.Class"),
            "void",
            Collections.emptyList());

    final TypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm cha = new ClassHierarchyAlgorithm(view, typeHierarchy);
    //    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));
    CallGraph cg = loadCallGraph("Misc", "update.operation.cg.Class");

    JavaClassType newClass =
        new JavaClassType("AdderA", identifierFactory.getPackageName("update.operation.cg"));
    CallGraph newCallGraph = cha.addClass(cg, newClass);

    assertEquals(0, cg.callsTo(mainMethodSignature));
    assertEquals(1, newCallGraph.callsTo(mainMethodSignature));

    assertEquals(1, cg.callsTo(methodSignature));
    assertEquals(3, newCallGraph.callsTo(methodSignature));
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
    assertEquals(2, cg.getMethodSignatures().size());

    assertTrue(cg.containsCall(mainMethodSignature, mainMethodSignature));
    assertTrue(cg.containsCall(mainMethodSignature, method));
    assertEquals(2, cg.callsFrom(mainMethodSignature).size());
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
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
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

    // TODO: ASK: isnt this more precise than CHA is? --> shouldnt it be vc3.Interface?
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
    // TODO: this callgraph looks strange!

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
}
