package de.upb.sse.sootup.test.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import de.upb.sse.sootup.callgraph.CallGraph;
import de.upb.sse.sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.typehierarchy.TypeHierarchy;
import de.upb.sse.sootup.java.core.views.JavaView;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Input source examples taken from https://bitbucket.org/delors/jcg/src/master/
 *
 * @author Markus Schmidt
 */
@Category(Java8Test.class)
public class ClassHierarchyAnalysisAlgorithmTest
    extends CallGraphTestBase<ClassHierarchyAnalysisAlgorithm> {

  // TODO: StaticInitializers, Lambdas ?

  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(
      JavaView view, TypeHierarchy typeHierarchy) {
    return new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
  }

  /**
   * Testing the call graph generation using CHA on a code example
   *
   * <p>In this testcase, the call graph of Example1 in folder {@link callgraph.Misc} is created
   * using CHA. The testcase expects a call from main to the constructors of B,C, and E The virtual
   * call print is resolved to all subtypes of A, A.print B.print, C.print, D.print and E.print,
   * Overall, 8 calls are expected in the main method. the constructor of B is called directly and
   * indirectly by C, since B is the super class of C.
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

    assertFalse(cg.containsCall(mainMethodSignature, constructorA));
    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));
    assertFalse(cg.containsCall(mainMethodSignature, constructorD));
    assertTrue(cg.containsCall(mainMethodSignature, constructorE));

    assertFalse(cg.containsMethod(staticMethodA));
    assertTrue(cg.containsCall(mainMethodSignature, staticMethodB));
    assertFalse(cg.containsMethod(staticMethodC));
    assertFalse(cg.containsMethod(staticMethodD));
    assertFalse(cg.containsMethod(staticMethodE));

    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodA));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodB));
    assertFalse(cg.containsMethod(virtualMethodC));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodD));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodE));

    assertEquals(8, cg.callsFrom(mainMethodSignature).size());

    assertEquals(1, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(constructorE).size());
    assertEquals(1, cg.callsTo(staticMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodA).size());
    assertEquals(1, cg.callsTo(virtualMethodB).size());
    assertEquals(1, cg.callsTo(virtualMethodD).size());
    assertEquals(1, cg.callsTo(virtualMethodE).size());

    assertEquals(0, cg.callsFrom(staticMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodA).size());
    assertEquals(0, cg.callsFrom(virtualMethodB).size());
    assertEquals(0, cg.callsFrom(virtualMethodD).size());
    assertEquals(0, cg.callsFrom(virtualMethodE).size());
  }
}
