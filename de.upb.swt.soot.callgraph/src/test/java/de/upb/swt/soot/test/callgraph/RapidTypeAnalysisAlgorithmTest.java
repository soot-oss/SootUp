package de.upb.swt.soot.test.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.CallGraph;
import de.upb.swt.soot.callgraph.RapidTypeAnalysisAlgorithm;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kadiray Karakaya, Jonas Klauke */
@Category(Java8Test.class)
public class RapidTypeAnalysisAlgorithmTest extends CallGraphTestBase<RapidTypeAnalysisAlgorithm> {

  @Override
  protected RapidTypeAnalysisAlgorithm createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    return new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
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

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodE =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.E"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));
    assertTrue(cg.containsCall(mainMethodSignature, constructorE));

    assertFalse(cg.containsCall(mainMethodSignature, constructorA));
    assertFalse(cg.containsCall(mainMethodSignature, constructorD));

    assertFalse(cg.containsMethod(methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertFalse(cg.containsMethod(methodD));
    assertTrue(cg.containsCall(mainMethodSignature, methodE));

    assertEquals(6, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(constructorE).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());
    assertEquals(1, cg.callsTo(methodE).size());

    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
    assertEquals(0, cg.callsFrom(methodE).size());
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

    assertFalse(cg.containsCall(alreadyVisitedMethod, newTargetA));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetB));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetC));
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

    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetA));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetB));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetC));
  }
}
