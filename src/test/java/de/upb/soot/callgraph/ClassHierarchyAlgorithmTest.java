package de.upb.soot.callgraph;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultIdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.core.*;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.JavaView;
import de.upb.soot.views.View;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author: Markus Schmidt */
@Category(Java8Test.class)
public class ClassHierarchyAlgorithmTest {

  // TODO: check if algo works in recursion
  // TODO: StaticInitializers?
  // TODO: Lambdas (recheck: Wala cant handle it currently?)

  private View view;
  DefaultIdentifierFactory identifierFactory = DefaultIdentifierFactory.getInstance();
  MethodSignature mainMethodSignature;
  JavaClassType declareClassSig;

  public CallGraph setup(String testDirectory, String className) {

    WalaClassLoader loader = new WalaClassLoader(testDirectory, null);

    AnalysisInputLocation inputLocation = loader.getAnalysisInputLocation();
    Project project = new Project(inputLocation);
    view = new JavaView<>(project);

    declareClassSig = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            "main", declareClassSig, "void", Collections.singletonList("java.lang.String[]"));

    Optional<SootMethod> m = WalaClassLoaderTestUtils.getSootMethod(loader, mainMethodSignature);
    assertTrue(m.isPresent());

    CallGraphAlgorithm cha = new ClassHierarchyAlgorithm(view, view.typeHierarchy());
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    System.out.println(cg.callsFrom(mainMethodSignature));

    return cg;
  }

  @Test
  public void testNonVirtualCall1() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/NonVirtualCall", "nvc1.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", declareClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testNonVirtualCall2() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/NonVirtualCall", "nvc2.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "Class", declareClassSig, "Class", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testNonVirtualCall3() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/NonVirtualCall", "nvc3.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method", declareClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testNonVirtualCall4() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/NonVirtualCall", "nvc4.Class");
    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            "method", declareClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, firstMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("nvc4.RootClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(firstMethod, targetMethod));
  }

  @Test
  public void testNonVirtualCall5() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/NonVirtualCall", "nvc5.Demo");

    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            "method", identifierFactory.getClassType("nvc4.Sub"), "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, firstMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("nvc4.RootClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(firstMethod, targetMethod));
  }

  @Test
  public void testVirtualCall1() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/VirtualCall", "vc1.Class");

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "target", declareClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod));
  }

  @Test
  public void testVirtualCall2() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/VirtualCall", "vc2.Class");

    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            "SubClass", declareClassSig, "SubClass", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "callMethod", declareClassSig, "void", Collections.singletonList("Class"));
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("vc2.SubClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(callMethod, targetMethod));
  }

  @Test
  public void testVirtualCall3() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/VirtualCall", "vc3.Class");

    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            "ClassImpl", declareClassSig, "vc3.ClassImpl", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "callOnInterface",
            declareClassSig,
            "void",
            Collections.singletonList("vc3.ClassImpl.Interface"));
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("vc3.ClassImpl"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(callMethod, targetMethod));
  }

  @Test
  public void testVirtualCall4() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/VirtualCall", "vc4.Class");

    // check static called constructors
    // TODO: check who calls them
    MethodSignature constructorMethod1 =
        identifierFactory.getMethodSignature(
            "ClassImpl", declareClassSig, "vc4.ClassImpl", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod1));

    MethodSignature constructorMethod2 =
        identifierFactory.getMethodSignature(
            "Class", declareClassSig, "vc4.Class", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, constructorMethod2));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "callOnInterface",
            declareClassSig,
            "void",
            Collections.singletonList("vc4.Class.Interface"));
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod1() {
    CallGraph cg =
        setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim1.Class");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method", declareClassSig, "void", Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod2() {
    CallGraph cg =
        setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim2.Class");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim2.SuperClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod3() {
    CallGraph cg =
        setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim3.Class");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim3.SuperClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, callMethod));
  }

  @Test
  public void testDynamicInterfaceMethod4() {
    CallGraph cg =
        setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim4.Class");

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
    CallGraph cg =
        setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim5.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method",
            identifierFactory.getClassType("j8dim5.DirectInterface"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, method));

    MethodSignature compute =
        identifierFactory.getMethodSignature(
            "compute",
            identifierFactory.getClassType("j8dim5.SuperClass"),
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, compute));
  }

  @Test
  public void testDynamicInterfaceMethod6() {
    CallGraph cg = setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8dim6.Demo");

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
    CallGraph cg = setup("src/test/resources/java-target/callgraph/InterfaceMethod", "j8sim.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            "method", identifierFactory.getClassType("Interface"), "void", Collections.emptyList());

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }
}
