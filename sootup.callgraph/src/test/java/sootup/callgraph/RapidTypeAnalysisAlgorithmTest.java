package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

/**
 * @author Kadiray Karakaya, Jonas Klauke
 */
public class RapidTypeAnalysisAlgorithmTest extends CallGraphTestBase<RapidTypeAnalysisAlgorithm> {

  @Override
  protected RapidTypeAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new RapidTypeAnalysisAlgorithm(view);
  }

  /**
   * Testing the call graph generation using RTA on a code example
   *
   * <p>In this testcase, the call graph of Example1 in folder {@link callgraph.Misc} is created
   * using RTA. The testcase expects a call from main to the constructors of B,C, and E The virtual
   * call print is resolved to calls to B.print, C.print, and E.print, since the types were
   * instantiated. Overall, 6 calls are expected in the main method. the constructor of B is called
   * directly and indirectly by C, since B is the super class of C.
   */
  @Test
  public void testMiscExample1() {
    CallGraph cg = loadCallGraph("Misc", "example1.Example");

    MethodSignature constructorA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature virtualMethodE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "virtualDispatch",
            "void",
            Collections.emptyList());

    MethodSignature staticMethodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature staticMethodE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "staticDispatch",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            constructorA,
            getInvokableStmt(mainMethodSignature, constructorB)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorB,
            getInvokableStmt(mainMethodSignature, constructorB)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorC,
            getInvokableStmt(mainMethodSignature, constructorC)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            constructorD,
            getInvokableStmt(mainMethodSignature, constructorC)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            constructorE,
            getInvokableStmt(mainMethodSignature, constructorE)));

    assertFalse(cg.containsMethod(staticMethodA));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            staticMethodB,
            getInvokableStmt(mainMethodSignature, staticMethodB)));
    assertFalse(cg.containsMethod(staticMethodC));
    assertFalse(cg.containsMethod(staticMethodD));
    assertFalse(cg.containsMethod(staticMethodE));

    assertFalse(cg.containsMethod(virtualMethodA));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodB,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertFalse(cg.containsMethod(virtualMethodC));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodD,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodE,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));

    assertEquals(1, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(constructorE).size());
    assertEquals(1, cg.callsTo(staticMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodD).size());
    assertEquals(1, cg.callsTo(virtualMethodE).size());

    assertEquals(0, cg.callsFrom(staticMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodD).size());
    assertEquals(0, cg.callsFrom(virtualMethodE).size());
  }

  @Test
  public void testRevisitMethod() {
    /* We expect a call edge from RevisitedMethod.alreadyVisitedMethod to A.newTarget, B.newTarget and C.newTarget*/
    CallGraph cg = loadCallGraph("Misc", "revisit.RevisitedMethod");

    MethodSignature alreadyVisitedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisit.RevisitedMethod"),
            "alreadyVisitedMethod",
            "void",
            Collections.singletonList("revisit.A"));

    MethodSignature newTargetA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisit.A"),
            "newTarget",
            "int",
            Collections.emptyList());
    MethodSignature newTargetB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisit.B"),
            "newTarget",
            "int",
            Collections.emptyList());
    MethodSignature newTargetC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisit.C"),
            "newTarget",
            "int",
            Collections.emptyList());

    assertFalse(
        cg.containsCall(
            alreadyVisitedMethod, newTargetA, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
    assertTrue(
        cg.containsCall(
            alreadyVisitedMethod, newTargetB, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
    assertTrue(
        cg.containsCall(
            alreadyVisitedMethod, newTargetC, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
  }

  @Test
  public void testRecursiveRevisitMethod() {
    /* We expect a call edge from RecursiveRevisitedMethod.alreadyVisitedMethod to A.newTarget, B.newTarget and C.newTarget*/
    CallGraph cg = loadCallGraph("Misc", "revisitrecur.RecursiveRevisitedMethod");

    MethodSignature alreadyVisitedMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisitrecur.RecursiveRevisitedMethod"),
            "recursiveAlreadyVisitedMethod",
            "void",
            Collections.singletonList("revisitrecur.A"));

    MethodSignature newTargetA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisitrecur.A"),
            "newTarget",
            "int",
            Collections.emptyList());
    MethodSignature newTargetB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisitrecur.B"),
            "newTarget",
            "int",
            Collections.emptyList());
    MethodSignature newTargetC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("revisitrecur.C"),
            "newTarget",
            "int",
            Collections.emptyList());

    assertFalse(
        cg.containsCall(
            alreadyVisitedMethod, newTargetA, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
    assertTrue(
        cg.containsCall(
            alreadyVisitedMethod, newTargetB, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
    assertTrue(
        cg.containsCall(
            alreadyVisitedMethod, newTargetC, getInvokableStmt(alreadyVisitedMethod, newTargetA)));
  }

  @Test
  public void testInstantiatedClassInClinit() {
    CallGraph cg = loadCallGraph("RTA", "cic.Class");
    MethodSignature instantiatedClassMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cic.SubClass"),
            "method",
            "void",
            Collections.emptyList());

    MethodSignature nonInstantiatedClassMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("cic.SuperClass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            instantiatedClassMethod,
            getInvokableStmt(mainMethodSignature, nonInstantiatedClassMethod)));
    assertFalse(
        cg.containsCall(
            mainMethodSignature,
            nonInstantiatedClassMethod,
            getInvokableStmt(mainMethodSignature, nonInstantiatedClassMethod)));
  }

  @Test
  public void testLaterInstantiatedClass() {
    CallGraph cg = loadCallGraph("RTA", "lic.Class");
    MethodSignature instantiatedClassMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("lic.InstantiatedClass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            instantiatedClassMethod,
            getInvokableStmt(mainMethodSignature, instantiatedClassMethod)));
  }
}
