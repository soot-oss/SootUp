package sootup.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import sootup.callgraph.AbstractCallGraphAlgorithm;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraph.Call;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class CallGraphTest {

  protected JavaIdentifierFactory identifierFactory;
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  private String algorithmName;
  private JavaView view;

  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
    if (algorithmName.equals("RTA")) {
      return new RapidTypeAnalysisAlgorithm(view);
    } else {
      return new ClassHierarchyAnalysisAlgorithm(view);
    }
  }

  private JavaView createViewForClassPath(String classPath) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath));
    return new JavaView(inputLocations);
  }

  CallGraph loadCallGraph() {
    //    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    //    if (version > 1.8) {
    //      fail("The rt.jar is not available after Java 8. You are using version " + version);
    //    }

    String classPath = "src/test/resources/callgraph/" + "Misc/binary";

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    view = createViewForClassPath(classPath);
    identifierFactory = view.getIdentifierFactory();

    mainClassSignature = identifierFactory.getClassType("Main");
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, identifierFactory.getMainSubSignature());

    SootClass sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(m, mainMethodSignature + " not found in classloader");

    AbstractCallGraphAlgorithm algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        cg.containsMethod(mainMethodSignature), mainMethodSignature + " is not found in CallGraph");
    return cg;
  }

  protected InvokableStmt getInvokableStmt(
      MethodSignature sourceMethod, MethodSignature staticTargetMethod) {
    SootMethod method = view.getMethod(sourceMethod).orElse(null);
    assertNotNull(method);
    for (Stmt stmt : method.getBody().getStmts()) {
      if (stmt.isInvokableStmt()
          && stmt.asInvokableStmt().getInvokeExpr().isPresent()
          && stmt.asInvokableStmt().getInvokeExpr().isPresent()
          && stmt.asInvokableStmt()
              .getInvokeExpr()
              .get()
              .getMethodSignature()
              .equals(staticTargetMethod)) {
        return stmt.asInvokableStmt();
      }
    }
    throw new RuntimeException("No invokable stmt found for " + sourceMethod);
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

    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodAbstract,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodMethodImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodMethodImplementedInstantiatedInSubClass,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodSubClassMethodNotImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodSubClassMethodImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));

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

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodInterface,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceImplementation,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceNoImplementation,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceImplementationNotInstatiated,
            getInvokableStmt(mainMethodSignature, methodInterface)));
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

    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodAbstract,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodMethodImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodMethodImplementedInstantiatedInSubClass,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodSubClassMethodNotImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodSubClassMethodImplemented,
            getInvokableStmt(mainMethodSignature, methodAbstract)));

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

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodInterface,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceImplementation,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceNoImplementation,
            getInvokableStmt(mainMethodSignature, methodInterface)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            methodInterfaceImplementationNotInstatiated,
            getInvokableStmt(mainMethodSignature, methodInterface)));
  }

  @Test
  public void dummyTest() {
    algorithmName = "CHA";
    loadCallGraph();
  }

  @Test
  public void checkCallGraphDotExporter() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph();
    // unsorted
    List<String> actualContent = cg.exportAsDot().toList();
    assertTrue(actualContent.size() > 1);
    assertEquals("strict digraph ObjectGraph {", actualContent.get(0));
    assertEquals("}", actualContent.get(actualContent.size() - 1));
    assertTrue(
        actualContent.contains(
            "\"<SubClassMethodNotImplemented: void <init>()>\"->\"<MethodImplementedInstantiatedInSubClass: void <init>()>\"[label=\"1\"]"));
    assertTrue(
        actualContent.contains(
            "\"<Main: void main(java.lang.String[])>\"->\"<InterfaceImplementation: int defaultMethod()>\"[label=\"9\"]"));
    assertTrue(
        actualContent.contains(
            "\"<Main: void main(java.lang.String[])>\"->\"<InterfaceNoImplementation: void <init>()>\"[label=\"7\"]"));
    assertTrue(
        actualContent.contains(
            "\"<Main: void main(java.lang.String[])>\"->\"<SubClassMethodImplemented: int method()>\"[label=\"5\"]"));

    // sorted
    List<String> actualContentSorted =
        cg.exportAsDot(
                Comparator.comparing(
                        (Call call) ->
                            call.sourceMethodSignature().getDeclClassType().getFullyQualifiedName())
                    // src method name
                    .thenComparing(call -> call.sourceMethodSignature().getName())
                    // src parameter list
                    .thenComparing(
                        call -> call.sourceMethodSignature().getParameterTypes().toString())
                    // target class name
                    .thenComparing(
                        call -> call.targetMethodSignature().getDeclClassType().getClassName())
                    // target method name
                    .thenComparing(call -> call.targetMethodSignature().getName())
                    // target parameter list
                    .thenComparing(
                        call -> call.targetMethodSignature().getParameterTypes().toString()))
            .toList();
    assertTrue(actualContentSorted.size() > 1);
    assertEquals("strict digraph ObjectGraph {", actualContentSorted.get(0));
    assertEquals("}", actualContentSorted.get(actualContentSorted.size() - 1));

    int call1 =
        actualContentSorted.indexOf(
            "\"<SubClassMethodNotImplemented: void <init>()>\"->\"<MethodImplementedInstantiatedInSubClass: void <init>()>\"[label=\"1\"]");
    int call2 =
        actualContentSorted.indexOf(
            "\"<Main: void main(java.lang.String[])>\"->\"<InterfaceImplementation: int defaultMethod()>\"[label=\"9\"]");
    int call3 =
        actualContentSorted.indexOf(
            "\"<Main: void main(java.lang.String[])>\"->\"<InterfaceNoImplementation: void <init>()>\"[label=\"7\"]");
    int call4 =
        actualContentSorted.indexOf(
            "\"<Main: void main(java.lang.String[])>\"->\"<SubClassMethodImplemented: int method()>\"[label=\"5\"]");
    assertTrue(call1 >= 0);
    assertTrue(call2 >= 0);
    assertTrue(call3 >= 0);
    assertTrue(call4 >= 0);

    List<Integer> sorted = Stream.of(call1, call2, call3, call4).sorted().toList();
    assertEquals(call2, sorted.get(0));
    assertEquals(call3, sorted.get(1));
    assertEquals(call4, sorted.get(2));
    assertEquals(call1, sorted.get(3));
  }
}
