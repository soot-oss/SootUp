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
  private String algorithmName;

  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
    if (algorithmName.equals("RTA")) {
      return new RapidTypeAnalysisAlgorithm(view);
    } else {
      return new ClassHierarchyAnalysisAlgorithm(view);
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

  CallGraph loadCallGraph() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath = "src/test/resources/callgraph/" + "Misc";

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    JavaView view = createViewForClassPath(classPath);

    mainClassSignature = identifierFactory.getClassType("Main");
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(mainMethodSignature + " not found in classloader", m);

    AbstractCallGraphAlgorithm algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    return cg;
  }

  @Test
  public void testRTA() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph();

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
    CallGraph cg = loadCallGraph();

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

  @Test
  public void checkCallGraphDotExporter() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph();
    String actualContent = cg.exportAsDot();
    String expectedContent =
        "strict digraph ObjectGraph {\n"
            + "\t\"<AbstractClass: void <init>()>\" -> \"<java.lang.Object: void <init>()>\";\n"
            + "\t\"<InterfaceImplementation: void <init>()>\" -> \"<java.lang.Object: void <init>()>\";\n"
            + "\t\"<InterfaceNoImplementation: void <init>()>\" -> \"<java.lang.Object: void <init>()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<Interface: int defaultMethod()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<InterfaceImplementation: void <init>()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<InterfaceImplementation: int defaultMethod()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<InterfaceNoImplementation: void <init>()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<MethodImplementedInstantiatedInSubClass: int method()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<java.lang.Object: void <clinit>()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<SubClassMethodImplemented: void <init>()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<SubClassMethodImplemented: int method()>\";\n"
            + "\t\"<Main: void main(java.lang.String[])>\" -> \"<SubClassMethodNotImplemented: void <init>()>\";\n"
            + "\t\"<MethodImplemented: void <init>()>\" -> \"<AbstractClass: void <init>()>\";\n"
            + "\t\"<MethodImplementedInstantiatedInSubClass: void <init>()>\" -> \"<AbstractClass: void <init>()>\";\n"
            + "\t\"<SubClassMethodImplemented: void <init>()>\" -> \"<MethodImplemented: void <init>()>\";\n"
            + "\t\"<SubClassMethodNotImplemented: void <init>()>\" -> \"<MethodImplementedInstantiatedInSubClass: void <init>()>\";\n"
            + "\t\"<java.lang.Object: void <clinit>()>\" -> \"<java.lang.Object: void <clinit>()>\";\n"
            + "\t\"<java.lang.Object: void <clinit>()>\" -> \"<java.lang.Object: void registerNatives()>\";\n}";
    assertEquals(actualContent, expectedContent);
  }
}
