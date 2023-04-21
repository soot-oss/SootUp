package sootup.tests;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

@Category(Java8Test.class)
public class CallGraphTest {

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  private AbstractCallGraphAlgorithm algorithm;
  private String algorithmName;

  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    if (algorithmName.equals("RTA")) {
      return new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    } else {
      return new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    }
  }

  private JavaView createViewForClassPath(String classPath) {
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    return javaProject.createView();
  }

  CallGraph loadCallGraph(String testDirectory, String className) {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath = "src/test/resources/callgraph/" + testDirectory;

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    JavaView view = createViewForClassPath(classPath);

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(mainMethodSignature + " not found in classloader", m);

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    algorithm = createAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    return cg;
  }

  @Test
  public void testRTA() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph("Misc", "Main");

    MethodSignature methodAbstract =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("AbstractClass"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodMethodImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("MethodImplemented"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodMethodImplementedInstantiatedInSubClass =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("MethodImplementedInstantiatedInSubClass"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodSubClassMethodImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("SubClassMethodImplemented"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodSubClassMethodNotImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("SubClassMethodNotImplemented"),
            "method",
            "int",
            Collections.emptyList());

    assertFalse(cg.containsCall(mainMethodSignature, methodAbstract));
    assertFalse(cg.containsCall(mainMethodSignature, methodMethodImplemented));
    assertTrue(cg.containsCall(mainMethodSignature, methodMethodImplementedInstantiatedInSubClass));
    assertFalse(cg.containsCall(mainMethodSignature, methodSubClassMethodNotImplemented));
    assertTrue(cg.containsCall(mainMethodSignature, methodSubClassMethodImplemented));

    MethodSignature methodInterface =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("Interface"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceNoImplementation =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceNoImplementation"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceImplementation =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceImplementation"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceImplementationNotInstatiated =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceImplementationNotInstatiated"),
            "defaultMethod",
            "int",
            Collections.emptyList());

    assertTrue(cg.containsCall(mainMethodSignature, methodInterface));
    assertTrue(cg.containsCall(mainMethodSignature, methodInterfaceImplementation));
    assertFalse(cg.containsCall(mainMethodSignature, methodInterfaceNoImplementation));
    assertFalse(cg.containsCall(mainMethodSignature, methodInterfaceImplementationNotInstatiated));
  }

  @Test
  public void testCHA() {
    algorithmName = "CHA";
    CallGraph cg = loadCallGraph("Misc", "Main");

    MethodSignature methodAbstract =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("AbstractClass"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodMethodImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("MethodImplemented"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodMethodImplementedInstantiatedInSubClass =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("MethodImplementedInstantiatedInSubClass"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodSubClassMethodImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("SubClassMethodImplemented"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature methodSubClassMethodNotImplemented =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("SubClassMethodNotImplemented"),
            "method",
            "int",
            Collections.emptyList());

    assertFalse(cg.containsCall(mainMethodSignature, methodAbstract));
    assertTrue(cg.containsCall(mainMethodSignature, methodMethodImplemented));
    assertTrue(cg.containsCall(mainMethodSignature, methodMethodImplementedInstantiatedInSubClass));
    assertFalse(cg.containsCall(mainMethodSignature, methodSubClassMethodNotImplemented));
    assertTrue(cg.containsCall(mainMethodSignature, methodSubClassMethodImplemented));

    MethodSignature methodInterface =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("Interface"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceNoImplementation =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceNoImplementation"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceImplementation =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceImplementation"),
            "defaultMethod",
            "int",
            Collections.emptyList());
    MethodSignature methodInterfaceImplementationNotInstatiated =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("InterfaceImplementationNotInstatiated"),
            "defaultMethod",
            "int",
            Collections.emptyList());

    assertTrue(cg.containsCall(mainMethodSignature, methodInterface));
    assertTrue(cg.containsCall(mainMethodSignature, methodInterfaceImplementation));
    assertFalse(cg.containsCall(mainMethodSignature, methodInterfaceNoImplementation));
    assertTrue(cg.containsCall(mainMethodSignature, methodInterfaceImplementationNotInstatiated));
  }
}
