package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public abstract class CallGraphAlgorithmTest extends CallGraphTest {

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

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            mainMethodSignature,
            getInvokableStmt(mainMethodSignature, mainMethodSignature)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature, method, getInvokableStmt(mainMethodSignature, method)));
  }

  @Test
  public void testConcreteCall() {
    CallGraph cg = loadCallGraph("ConcreteCall", "cvc.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvcsc.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvcscddi.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvcscwi.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvci.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvcsi.Class");
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
    CallGraph cg = loadCallGraph("ConcreteCall", "cvcscsi.Class");
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
    MethodSignature invokedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("nvc4.Superclass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(firstMethod, targetMethod, getInvokableStmt(firstMethod, invokedMethod)));
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
  public void testSuperCall() {
    CallGraph cg = loadCallGraph("NonVirtualCall", "supercall.Demo");
    MethodSignature sourceMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "source", "void", Collections.emptyList());
    assertTrue(cg.containsMethod(sourceMethod));
    // this method does not exist, but it is the dispatch signature of the super call
    MethodSignature superMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("supercall.SuperClass"),
            "method",
            "void",
            Collections.emptyList());
    assertFalse(cg.containsMethod(superMethod));
    // check if the correct call is contained
    // the wrong call is already checked by the missing method node in the previous check.
    MethodSignature superSuperMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("supercall.SuperSuperClass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            sourceMethod, superSuperMethod, getInvokableStmt(sourceMethod, superMethod)));
    // supermethod is used in getInvokableStmt since it is the dispatch signature
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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim0.Class");
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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim1.Class");
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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim2.SuperClass");

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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim3.SuperClass");

    MethodSignature callMethod =
        identifierFactory.getMethodSignature(
            mainClassSignature, "method", "void", Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature, callMethod, getInvokableStmt(mainMethodSignature, callMethod)));
  }

  @Test
  public void testDynamicInterfaceMethod4() {
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim4.SuperClass");

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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim5.SuperClass");

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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8dim6.Demo");

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
    CallGraph cg = loadCallGraph("InterfaceMethod", "j8sim.Class");

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
    JavaView view = createViewForClassPath("src/test/resources/callgraph/DefaultEntryPoint/binary");

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

    JavaView view = createViewForClassPath("src/test/resources/callgraph/Misc/binary");

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

    JavaView view = createViewForClassPath("src/test/resources/callgraph/NoMainMethod/binary");

    CallGraphAlgorithm algorithm = createAlgorithm(view);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when no main methods are defined.");
    } catch (RuntimeException e) {
      assertEquals(
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.",
          e.getMessage());
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
            "int",
            Collections.emptyList());
    MethodSignature staticMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.MultiCalls"),
            "method",
            "int",
            Collections.emptyList());
    MethodSignature staticMethodField =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("multi.FieldLeft"),
            "method",
            "int",
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

    checkClinit(cg, "multi.Instantiated", false, null);

    checkClinit(cg, "multi.FieldLeft", true, null);

    checkClinit(cg, "multi.FieldRight", false, null);

    checkClinit(cg, "multi.MultiCalls", false, staticMethod);

    assertEquals(11, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(staticMethod).size());
    assertEquals(1, cg.callsTo(staticMethodField).size());
    assertEquals(2, cg.callsTo(constructorMethod).size());
    assertEquals(2, cg.callsTo(virtualMethod).size());

    assertEquals(0, cg.callsFrom(staticMethod).size());
    assertEquals(0, cg.callsFrom(staticMethodField).size());
    assertEquals(1, cg.callsFrom(constructorMethod).size());
    assertEquals(0, cg.callsFrom(virtualMethod).size());
  }

  private void checkClinit(CallGraph cg, String clinitClassName, boolean left, MethodSignature ms) {
    ClassType clinitClass = identifierFactory.getClassType(clinitClassName);
    MethodSignature clinitMethod = identifierFactory.getStaticInitializerSignature(clinitClass);
    InvokableStmt invokeStmt;
    if (ms == null) {
      invokeStmt = getInvokableStmtNonInvokeExpr(mainMethodSignature, clinitClass, left, 0);
    } else {
      invokeStmt = getInvokableStmt(mainMethodSignature, ms, 0);
    }
    assertTrue(cg.containsCall(mainMethodSignature, clinitMethod, invokeStmt));
  }

  @Test
  public void testIssue903() {
    CallGraph cg = loadCallGraph("Bugfixes", "issue903.B");

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

  @Test
  public void testImplicitExample1() {
    CallGraph cg = loadCallGraph("Implicit", "t1.Example1");
    MethodSignature updatedRunMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t1.UpdatedThread1"),
            "run",
            "void",
            Collections.emptyList());
    Set<MethodSignature> callSourcesMethodSigs = cg.callSourcesTo(updatedRunMethodSig);
    assertTrue(callSourcesMethodSigs.contains(mainMethodSignature));
  }

  @Test
  public void testImplicitExample2() {
    CallGraph cg = loadCallGraph("Implicit", "t2.Example2");
    MethodSignature updatedRunMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t2.UpdatedThreadInner"),
            "run",
            "void",
            Collections.emptyList());
    Set<MethodSignature> callSourcesMethodSigs = cg.callSourcesTo(updatedRunMethodSig);
    assertTrue(callSourcesMethodSigs.contains(mainMethodSignature));
  }

  @Test
  public void testImplicitExample3() {
    CallGraph cg = loadCallGraph("Implicit", "t3.Example3");
    MethodSignature updatedRunMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t3.UpdatedThreadOuter"),
            "run",
            "void",
            Collections.emptyList());
    Set<MethodSignature> callSourcesMethodSigs = cg.callSourcesTo(updatedRunMethodSig);
    assertTrue(callSourcesMethodSigs.contains(mainMethodSignature));
  }

  @Test
  public void testImplicitExample4() {
    CallGraph cg = loadCallGraph("Implicit", "t4.Example4");
    MethodSignature updatedRunMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t4.UpdatedThreadInner"),
            "run",
            "void",
            Collections.emptyList());
    Set<MethodSignature> callSourcesMethodSigs = cg.callSourcesTo(updatedRunMethodSig);
    assertTrue(callSourcesMethodSigs.contains(mainMethodSignature));
  }

  @Test
  public void testImplicitExample5() {
    CallGraph cg = loadCallGraph("Implicit", "t5.Example5");
    MethodSignature updatedRunMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t5.UpdatedThread2"),
            "run",
            "void",
            Collections.emptyList());
    Set<MethodSignature> callSourcesMethodSigs = cg.callSourcesTo(updatedRunMethodSig);
    assertTrue(callSourcesMethodSigs.contains(mainMethodSignature));
  }

  @Test
  public void testImplicitExample6() {
    CallGraph cg = loadCallGraph("Implicit", "t6.Example6");
    MethodSignature runMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("t6.NoThread"), "run", "void", Collections.emptyList());
    assertFalse(cg.containsMethod(runMethodSig));
  }

  @Test
  public void testTruePruning() {
    // includeCall() always true --> all methods and their calls are included
    CallGraph cg = loadCallGraph("Misc", "prune.Pruning");
    MethodSignature pruned =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("prune.Pruning"),
            "methodB",
            "void",
            Collections.emptyList());
    assertTrue(cg.containsMethod(pruned));
  }

  @Test
  public void testIssue1373() {
    CallGraph cg = loadCallGraph("Bugfixes", "issue1373.B");
    for (CallGraph.Call call : cg.getCalls()) {
      assertNotEquals(call.sourceMethodSignature(), call.targetMethodSignature());
    }
  }

  @Test
  public void testClinitCallPruning() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccp.Main");
    IdentifierFactory id = view.getIdentifierFactory();
    MethodSignature methodSigOperation =
        id.getMethodSignature(
            "ccp.ClinitCallPruning$Operation", "<clinit>", "void", Collections.emptyList());
    // no duplicates
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigOperation))
            .count());
    // no self-calls
    for (CallGraph.Call call : cg.getCalls()) {
      assertNotEquals(call.sourceMethodSignature(), call.targetMethodSignature());
    }
  }

  @Test
  public void testClinitCallPruning2() {
    CallGraph cg = loadCallGraph("ClinitCall", "ccp2.ClinitCallPruning2");

    for (CallGraph.Call call : cg.getCalls()) {
      System.out.println(call);
    }

    IdentifierFactory id = view.getIdentifierFactory();

    // testClinitCallPruningChild
    MethodSignature methodSigChild =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningChild", "<clinit>", "void", Collections.emptyList());
    MethodSignature methodSigParent =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningParent", "<clinit>", "void", Collections.emptyList());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigChild))
            .count());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(
                call ->
                    call.targetMethodSignature().equals(methodSigParent)
                        && call.invokableStmt().toString().contains("Parent"))
            .count());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigParent))
            .count());

    // testClinitCallPruningChild2
    MethodSignature methodSigChild2 =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningChild2", "<clinit>", "void", Collections.emptyList());
    MethodSignature methodSigParent2 =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningParent2", "<clinit>", "void", Collections.emptyList());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigChild2))
            .count());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(
                call ->
                    call.targetMethodSignature().equals(methodSigParent2)
                        && call.invokableStmt().toString().contains("Child"))
            .count());
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigParent2))
            .count());

    // testClinitCallPruningSelf
    // no self-call
    for (CallGraph.Call call : cg.getCalls()) {
      assertNotEquals(call.sourceMethodSignature(), call.targetMethodSignature());
    }

    // testClinitCallPruningBranch
    MethodSignature methodSigOperation =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningBranch", "<clinit>", "void", Collections.emptyList());
    // include duplicate (method invocations within if-block)
    assertEquals(
        2,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigOperation))
            .count());

    // testClinitCallPruningBranch2
    MethodSignature methodSigOperation2 =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningBranch2", "<clinit>", "void", Collections.emptyList());
    // do not include duplicate (method invocation before if-block)
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigOperation2))
            .count());

    // testClinitCallPruningBranch3
    MethodSignature methodSigOperation3 =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningBranch3", "<clinit>", "void", Collections.emptyList());
    // do not include duplicate (method invocation before while-loop)
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigOperation3))
            .count());

    // testClinitCallPruningBranch4
    MethodSignature methodSigOperation4 =
        id.getMethodSignature(
            "ccp2.ClinitCallPruningBranch4", "<clinit>", "void", Collections.emptyList());
    // do not include duplicate
    assertEquals(
        1,
        cg.callsFrom(mainMethodSignature).stream()
            .filter(call -> call.targetMethodSignature().equals(methodSigOperation4))
            .count());
  }
}
