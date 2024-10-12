package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.frontend.inputlocation.JavaSourcePathAnalysisInputLocation;

public abstract class CallGraphTestBase<T extends AbstractCallGraphAlgorithm> {

  private T algorithm;
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  protected JavaView view;

  protected abstract T createAlgorithm(JavaView view);

  // private static Map<String, JavaView> viewToClassPath = new HashMap<>();

  private JavaView createViewForClassPath(String classPath) {
    return createViewForClassPath(classPath, true);
  }

  private JavaView createViewForClassPath(String classPath, boolean useSourceCodeFrontend) {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    if (useSourceCodeFrontend) {
      inputLocations.add(new JavaSourcePathAnalysisInputLocation(classPath));
    } else {
      inputLocations.add(new JavaClassPathAnalysisInputLocation(classPath));
    }

    return new JavaView(inputLocations);
  }

  CallGraph loadCallGraph(String testDirectory, String className) {
    return loadCallGraph(testDirectory, true, className);
  }

  CallGraph loadCallGraph(String testDirectory, boolean useSourceCodeFrontend, String className) {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath =
        "src/test/resources/callgraph/"
            + testDirectory
            + "/"
            + (useSourceCodeFrontend ? "source" : "binary");

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    view = createViewForClassPath(classPath, useSourceCodeFrontend);

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    SootClass sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(m, mainMethodSignature + " not found in classloader");

    algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertNotNull(cg);
    assertTrue(
        cg.containsMethod(mainMethodSignature), mainMethodSignature + " is not found in CallGraph");
    return cg;
  }

  protected InvokableStmt getInvokableStmt(
      MethodSignature sourceMethod, MethodSignature staticTargetMethod) {
    return getInvokableStmt(sourceMethod, staticTargetMethod, 0);
  }

  protected InvokableStmt getInvokableStmt(
      MethodSignature sourceMethod, MethodSignature staticTargetMethod, int index) {
    int currentIndex = 0;
    SootMethod method = view.getMethod(sourceMethod).orElse(null);
    assertNotNull(method);
    for (Stmt invokableStmt : method.getBody().getStmts()) {
      if (invokableStmt instanceof InvokableStmt
          && ((InvokableStmt) invokableStmt).containsInvokeExpr()
          && ((InvokableStmt) invokableStmt)
              .getInvokeExpr()
              .get()
              .getMethodSignature()
              .equals(staticTargetMethod)) {
        if (currentIndex == index) {
          return (InvokableStmt) invokableStmt;
        }
        currentIndex++;
      }
    }
    throw new RuntimeException(
        "No invokable stmt of method " + staticTargetMethod + " found for " + sourceMethod);
  }

  protected InvokableStmt getInvokableStmtNonInvokeExpr(
      MethodSignature sourceMethod, ClassType targetClass) {
    return getInvokableStmtNonInvokeExpr(sourceMethod, targetClass, false, 0);
  }

  protected InvokableStmt getInvokableStmtNonInvokeExpr(
      MethodSignature sourceMethod, ClassType targetClass, boolean leftExpr, int index) {
    int currentIndex = 0;
    SootMethod method = view.getMethod(sourceMethod).orElse(null);
    assertNotNull(method);

    InstantiateClassValueVisitor instantiateVisitor = new InstantiateClassValueVisitor();
    for (Stmt invokableStmt : method.getBody().getStmts()) {
      // look only at assigments which do Invoke but does not contain a direct invoke expr
      // static fields and new array expressions
      if (invokableStmt instanceof JAssignStmt
          && !((InvokableStmt) invokableStmt).containsInvokeExpr()
          && ((InvokableStmt) invokableStmt).invokesStaticInitializer()) {
        Value expr;
        // look at the left or right side of the assigment
        if (leftExpr) {
          expr = ((JAssignStmt) invokableStmt).getLeftOp();
        } else {
          expr = ((JAssignStmt) invokableStmt).getRightOp();
        }
        // extract the class type
        ClassType classType = null;
        if (expr instanceof JFieldRef) {
          classType = ((JFieldRef) expr).getFieldSignature().getDeclClassType();
        } else if (expr instanceof JNewExpr
            || expr instanceof JNewArrayExpr
            || expr instanceof JNewMultiArrayExpr) {
          instantiateVisitor.init();
          expr.accept(instantiateVisitor);
          classType = instantiateVisitor.getResult();
        }
        // probably caused by invoke caused on the other operand side
        if (classType == null) {
          continue;
        }
        if (classType.equals(targetClass)) {
          // found fitting stmt in given position
          if (currentIndex == index) {
            return (InvokableStmt) invokableStmt;
          }
          // search next fitting stmt
          currentIndex++;
        }
      }
    }
    throw new RuntimeException(
        "No invokable assignment stmt of class " + targetClass + " found for " + sourceMethod);
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
    CallGraph cg = loadCallGraph("Misc", "update.operation.cg.Class");

    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("update.operation.cg.Class"),
            "method",
            "void",
            Collections.emptyList());

    JavaClassType newClass =
        new JavaClassType("AdderA", identifierFactory.getPackageName("update.operation.cg"));
    CallGraph newCallGraph = algorithm.addClass(cg, newClass);

    assertEquals(0, cg.callsTo(mainMethodSignature).size());
    assertEquals(1, newCallGraph.callsTo(mainMethodSignature).size());

    assertEquals(1, cg.callsTo(methodSignature).size());
    assertEquals(3, newCallGraph.callsTo(methodSignature).size());
  }

  @Test
  public void testRecursiveCall() {
    CallGraph cg = loadCallGraph("Misc", "recur.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());

    MethodSignature uncalledMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.singletonList("int"));

    assertTrue(cg.containsMethod(mainMethodSignature));
    assertTrue(cg.containsMethod(method));
    assertFalse(cg.containsMethod(uncalledMethod));
    // 2 methods + Object::clinit
    assertEquals(3, cg.getMethodSignatures().size());

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            mainMethodSignature,
            getInvokableStmt(mainMethodSignature, mainMethodSignature)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, method)));
    // 2 calls +2 clinit calls
    assertEquals(4, cg.callsFrom(mainMethodSignature).size());
  }

  @Test
  public void testConcreteCall() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvc.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvc.Class"), "target", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethod)));
  }

  @Test
  public void testConcreteCallInSuperClass() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvcsc.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcsc.SuperClass"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcsc.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethodInvoked)));
  }

  @Test
  public void testConcreteCallDifferentDefaultMethodInSubClass() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvcscddi.Class");
    MethodSignature interfaceMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscddi.Interface"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature subInterfaceMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscddi.SubInterface"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscddi.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            interfaceMethod,
            getInvokableStmt(mainMethodSignature, invokedMethod)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            subInterfaceMethod,
            getInvokableStmt(mainMethodSignature, invokedMethod)));
  }

  @Test
  public void testConcreteCallInSuperClassWithDefaultInterface() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvcscwi.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscwi.SuperClass"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscwi.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethodInvoked)));
  }

  @Test
  public void testConcreteCallInInterface() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvci.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvci.Interface"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvci.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, invokedMethod)));
  }

  @Test
  public void testConcreteCallInSubInterface() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvcsi.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcsi.SubInterface"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcsi.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethodInvoked)));
  }

  @Test
  public void testConcreteCallInSuperClassSubInterface() {
    CallGraph cg = loadCallGraph("ConcreteCall", false, "cvcscsi.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscsi.SubInterface"),
            "target",
            "void",
            Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cvcscsi.Class"),
            "target",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethodInvoked)));
  }

  @Test
  public void testClinitCallEntryPoint() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccep.Class");
    MethodSignature targetMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccep.Class"));
    assertTrue(cg.getEntryMethods().contains(targetMethod));
  }

  @Test
  public void testClinitCallProcessed() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccp.Class");
    MethodSignature sourceMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccp.Class"));
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("ccp.Class"), "method", "int", Collections.emptyList());
    assertTrue(
        cg.containsCall(sourceMethod, targetMethod, getInvokableStmt(sourceMethod, targetMethod)));
  }

  @Test
  public void testClinitCallConstructor() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccc.Class");
    MethodSignature targetMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccc.DirectType"));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmtNonInvokeExpr(
                mainMethodSignature, identifierFactory.getClassType("ccc.DirectType"))));

    MethodSignature arrayMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccc.ArrayType"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            arrayMethod,
            getInvokableStmtNonInvokeExpr(
                mainMethodSignature, identifierFactory.getClassType("ccc.ArrayType"))));

    MethodSignature arrayDimMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccc.ArrayDimType"));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            arrayDimMethod,
            getInvokableStmtNonInvokeExpr(
                mainMethodSignature, identifierFactory.getClassType("ccc.ArrayDimType"))));

    MethodSignature arrayInArrayMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccc.ArrayInArrayType"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            arrayInArrayMethod,
            getInvokableStmtNonInvokeExpr(
                mainMethodSignature, identifierFactory.getClassType("ccc.ArrayInArrayType"))));
  }

  @Test
  public void testClinitCallSuperConstructor() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccsc.Class");
    MethodSignature targetMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccsc.Clinit"));
    InvokableStmt invokedStmt =
        getInvokableStmtNonInvokeExpr(
            mainMethodSignature, identifierFactory.getClassType("ccsc.Clinit"));
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod, invokedStmt));
    MethodSignature targetMethod2 =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccsc.SuperClinit"));
    assertTrue(cg.containsCall(mainMethodSignature, targetMethod2, invokedStmt));
  }

  @Test
  public void testClinitStaticMethodCall() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccsmc.Class");
    MethodSignature targetMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccsmc.Clinit"));
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("ccsmc.Clinit"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethodInvoked)));
  }

  @Test
  public void testClinitStaticField() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccsf.Class");
    MethodSignature targetMethod =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("ccsf.Clinit"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmtNonInvokeExpr(mainMethodSignature, targetMethod.getDeclClassType())));
  }

  @Test
  public void testNonVirtualCall1() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc1.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethod)));
  }

  @Test
  public void testNonVirtualCall2() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc2.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "<init>", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethod)));
  }

  @Test
  public void testNonVirtualCall3() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc3.Class");
    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    MethodSignature uncalledMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.singletonList("int"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethod)));
    assertFalse(cg.containsMethod(uncalledMethod));
  }

  @Test
  public void testNonVirtualCall4() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc4.Class");
    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, firstMethod, getInvokableStmt(mainMethodSignature, firstMethod)));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("nvc4.Rootclass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(firstMethod, targetMethod, getInvokableStmt(firstMethod, targetMethod)));
  }

  @Test
  public void testNonVirtualCall5() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "nvc5.Demo");

    MethodSignature firstMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("nvc5.Sub"), "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, firstMethod, getInvokableStmt(mainMethodSignature, firstMethod)));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("nvc5.Middle"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(firstMethod, targetMethod, getInvokableStmt(firstMethod, targetMethod)));
  }

  @Test
  public void testVirtualCall1() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc1.Class");

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "target", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            targetMethod,
            getInvokableStmt(mainMethodSignature, targetMethod)));
  }

  @Test
  public void testVirtualCall2() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc2.Class");

    JavaClassType subClassSig = identifierFactory.getClassType("vc2.SubClass");
    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            subClassSig, "<init>", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorMethod,
            getInvokableStmt(mainMethodSignature, constructorMethod)));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "callMethod", "void", Collections.singletonList("vc2.Class"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, callMethod)));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            subClassSig, "method", "void", Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            callMethod, targetMethod, getInvokableStmt(callMethod, targetMethodInvoked)));
  }

  @Test
  public void testVirtualCall3() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc3.Class");

    JavaClassType subClassSig = identifierFactory.getClassType("vc3.ClassImpl");
    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            subClassSig, "<init>", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorMethod,
            getInvokableStmt(mainMethodSignature, constructorMethod)));

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature,
            "callOnInterface",
            "void",
            Collections.singletonList("vc3.Interface"));
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, callMethod)));

    MethodSignature targetMethod =
        identifierFactory.getMethodSignature(
            subClassSig, "method", "void", Collections.emptyList());
    MethodSignature targetMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("vc3.Interface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            callMethod, targetMethod, getInvokableStmt(callMethod, targetMethodInvoked)));
  }

  @Test
  public void testVirtualCall4() {
    CallGraph cg = loadCallGraph("VirtualCall", "vc4.Class");

    // more precise its: declareClassSig
    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("vc4.Class"), "method", "void", Collections.emptyList());
    MethodSignature callMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("vc4.Interface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            callMethod,
            getInvokableStmt(mainMethodSignature, callMethodInvoked)));
  }

  @Test
  public void testDynamicInterfaceMethod0() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim0.Class");
    MethodSignature interfaceMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim0.Interface"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature classMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim0.Class"),
            "method",
            "void",
            Collections.emptyList());
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            interfaceMethod,
            getInvokableStmt(mainMethodSignature, interfaceMethod)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            classMethod,
            getInvokableStmt(mainMethodSignature, interfaceMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod1() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim1.Class");
    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim1.Interface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, callMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod2() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim2.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim2.SuperClass"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim2.Interface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, invokedMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod3() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim3.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, callMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod4() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim4.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim4.Interface"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim4.SubClass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, invokedMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod5() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim5.SuperClass");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim5.DirectInterface"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim5.Class"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, invokedMethod)));

    MethodSignature compute =
        identifierFactory.getMethodSignature(
            mainClassSignature, "compute", "void", Collections.emptyList());
    MethodSignature invokedCompute =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim5.Class"),
            "compute",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, compute, getInvokableStmt(mainMethodSignature, invokedCompute)));
  }

  @Test
  public void testDynamicInterfaceMethod6() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8dim6.Demo");

    MethodSignature combinedInterfaceMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim6.CombinedInterface"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature combinedInterfaceMethodInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim6.Demo$1"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            combinedInterfaceMethod,
            getInvokableStmt(mainMethodSignature, combinedInterfaceMethodInvoked)));

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim6.SomeInterface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            combinedInterfaceMethod, method, getInvokableStmt(combinedInterfaceMethod, method)));

    MethodSignature anotherMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8dim6.AnotherInterface"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            combinedInterfaceMethod,
            anotherMethod,
            getInvokableStmt(combinedInterfaceMethod, anotherMethod)));
  }

  @Test
  public void testStaticInterfaceMethod() {
    CallGraph cg = loadCallGraph("InterfaceMethod", false, "j8sim.Class");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("j8sim.Interface"),
            "method",
            "void",
            Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, method)));
  }

  @Test
  public void testAbstractMethod() {
    CallGraph cg = loadCallGraph("AbstractMethod", "am1.Main");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am1.Class"), "method", "void", Collections.emptyList());

    MethodSignature abstractMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am1.AbstractClass"),
            "method",
            "void",
            Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, abstractMethod)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            abstractMethod,
            getInvokableStmt(mainMethodSignature, abstractMethod)));
  }

  @Test
  public void testAbstractMethodInSubClass() {
    CallGraph cg = loadCallGraph("AbstractMethod", "am2.Main");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am2.Class"), "method", "void", Collections.emptyList());

    MethodSignature abstractMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am2.AbstractClass"),
            "method",
            "void",
            Collections.emptyList());
    MethodSignature superMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am2.SuperClass"),
            "method",
            "void",
            Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, superMethod)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            abstractMethod,
            getInvokableStmt(mainMethodSignature, superMethod)));

    if (this instanceof ClassHierarchyAnalysisAlgorithmTest) {
      assertTrue(
          cg.containsCall(
              mainMethodSignature,
              superMethod,
              getInvokableStmt(mainMethodSignature, superMethod)));
    }
    if (this instanceof RapidTypeAnalysisAlgorithmTest) {
      assertFalse(
          cg.containsCall(
              mainMethodSignature,
              superMethod,
              getInvokableStmt(mainMethodSignature, superMethod)));
    }
  }

  @Test
  public void testAbstractMethodMissingMethodInSuperclass() {
    CallGraph cg = loadCallGraph("AbstractMethod", "am3.Main");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am3.Class"), "method", "void", Collections.emptyList());

    MethodSignature superMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am3.SuperClass"),
            "method",
            "void",
            Collections.emptyList());

    MethodSignature abstractMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("am3.AbstractClass"),
            "method",
            "void",
            Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, superMethod)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            abstractMethod,
            getInvokableStmt(mainMethodSignature, superMethod)));
  }

  @Test
  public void testWithoutEntryMethod() {
    JavaView view = createViewForClassPath("src/test/resources/callgraph/DefaultEntryPoint");

    JavaClassType mainClassSignature = identifierFactory.getClassType("example2.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    CallGraphAlgorithm algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize();
    assertTrue(
        cg.containsMethod(mainMethodSignature), mainMethodSignature + " is not found in CallGraph");
    assertNotNull(cg);
  }

  /**
   * Test uses initialize() method to create call graph, but multiple main methods are present in
   * input java source files. Expected result is RuntimeException.
   */
  @Test
  public void testMultipleMainMethod() {

    JavaView view = createViewForClassPath("src/test/resources/callgraph/Misc");

    CallGraphAlgorithm algorithm = createAlgorithm(view);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when multiple main methods are defined.");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().startsWith("There are more than 1 main method present"));
    }
  }

  /**
   * Test uses initialize() method to create call graph, but no main method is present in input java
   * source files. Expected result is RuntimeException.
   */
  @Test
  public void testNoMainMethod() {

    JavaView view = createViewForClassPath("src/test/resources/callgraph/NoMainMethod");

    CallGraphAlgorithm algorithm = createAlgorithm(view);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when no main methods are defined.");
    } catch (RuntimeException e) {
      assertEquals(
          e.getMessage(),
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }
  }

  /**
   * Test uses initialize() method to create call graph, but no main method is present in input java
   * source files. Expected result is RuntimeException.
   */
  @Test
  public void testStopAtLibraryClass() {

    String classPath = "src/test/resources/callgraph/Library/binary/";

    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "application/", SourceType.Application));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "library/", SourceType.Library));

    JavaView view = new JavaView(inputLocations);

    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "app.Application", "main", "void", Collections.singletonList("java.lang.String[]"));
    CallGraphAlgorithm algorithm = createAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertFalse(cg.callsFrom(mainMethodSignature).isEmpty());

    SootClass libraryClass =
        view.getClass(view.getIdentifierFactory().getClassType("lib.Library")).orElse(null);
    assertNotNull(libraryClass);
    for (SootMethod method : libraryClass.getMethods()) {
      MethodSignature ms = method.getSignature();
      if (cg.containsMethod(ms)) {
        assertEquals(0, cg.callsFrom(method.getSignature()).size());
      }
    }
  }

  @Test
  public void testMultiCallsToSameTarget() {
    CallGraph cg = loadCallGraph("Misc", "multi.MultipleCallsToSameTarget");

    MethodSignature constructorMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.Instantiated"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.Instantiated"),
            "method",
            "void",
            Collections.emptyList());

    MethodSignature staticMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.MultiCalls"),
            "method",
            "void",
            Collections.emptyList());

    MethodSignature staticMethodField =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.FieldLeft"),
            "method",
            "void",
            Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorMethod,
            getInvokableStmt(mainMethodSignature, constructorMethod, 0)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorMethod,
            getInvokableStmt(mainMethodSignature, constructorMethod, 1)));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethod,
            getInvokableStmt(mainMethodSignature, staticMethod, 0)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethod,
            getInvokableStmt(mainMethodSignature, staticMethod, 1)));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethodField,
            getInvokableStmt(mainMethodSignature, staticMethodField, 0)));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethod,
            getInvokableStmt(mainMethodSignature, virtualMethod, 0)));

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethod,
            getInvokableStmt(mainMethodSignature, virtualMethod, 1)));

    checkClinit(cg, "multi.Instantiated", 0, false, null);
    checkClinit(cg, "multi.Instantiated", 1, false, null);

    checkClinit(cg, "multi.FieldLeft", 0, true, null);
    checkClinit(cg, "multi.FieldLeft", 1, true, null);
    checkClinit(cg, "multi.FieldLeft", 2, true, null);

    checkClinit(cg, "multi.FieldRight", 0, false, null);
    checkClinit(cg, "multi.FieldRight", 1, false, null);

    checkClinit(cg, "multi.MultiCalls", 0, false, staticMethod);
    checkClinit(cg, "multi.MultiCalls", 1, false, staticMethod);

    assertEquals(27, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(staticMethod).size());
    assertEquals(1, cg.callsTo(staticMethodField).size());
    assertEquals(2, cg.callsTo(constructorMethod).size());
    assertEquals(2, cg.callsTo(virtualMethod).size());

    assertEquals(0, cg.callsFrom(staticMethod).size());
    assertEquals(0, cg.callsFrom(staticMethodField).size());
    assertEquals(1, cg.callsFrom(constructorMethod).size());
    assertEquals(0, cg.callsFrom(virtualMethod).size());
  }

  private void checkClinit(
      CallGraph cg, String clinitClassName, int index, boolean left, MethodSignature ms) {
    ClassType clinitClass = identifierFactory.getClassType(clinitClassName);
    MethodSignature clinitMethod = identifierFactory.getStaticInitializerSignature(clinitClass);
    MethodSignature clinitObject =
        identifierFactory.getStaticInitializerSignature(
            identifierFactory.getClassType("java.lang.Object"));
    InvokableStmt invokeStmt;
    if (ms == null) {
      invokeStmt = getInvokableStmtNonInvokeExpr(mainMethodSignature, clinitClass, left, index);
    } else {
      invokeStmt = getInvokableStmt(mainMethodSignature, ms, index);
    }
    assertTrue(cg.containsCall(mainMethodSignature, clinitObject, invokeStmt));
    assertTrue(cg.containsCall(mainMethodSignature, clinitMethod, invokeStmt));
  }

  @Test
  public void testIssue903() {
    CallGraph cg = loadCallGraph("Bugfixes", false, "issue903.B");

    MethodSignature closingCall =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("issue903.A"), "close", "void", Collections.emptyList());
    MethodSignature closingCallInvoked =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("issue903.B"), "close", "void", Collections.emptyList());

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            closingCall,
            getInvokableStmt(mainMethodSignature, closingCallInvoked, 0)));

    assertTrue(
        cg.containsCall(closingCall, closingCall, getInvokableStmt(closingCall, closingCall, 0)));
  }
}
