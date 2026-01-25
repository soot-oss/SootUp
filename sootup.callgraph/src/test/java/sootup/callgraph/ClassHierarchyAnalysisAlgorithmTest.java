package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

/**
 * Input source examples taken from <a href="https://bitbucket.org/delors/cats/src/master/">cats</a>
 *
 * @author Markus Schmidt
 */
public class ClassHierarchyAnalysisAlgorithmTest extends CallGraphAlgorithmTest {

  // TODO: StaticInitializers, Lambdas ?

  @Override
  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new ClassHierarchyAnalysisAlgorithm(view);
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

    assertTrue(
        cg.containsCall(
            mainMethodSignature,
            virtualMethodA,
            getInvokableStmt(mainMethodSignature, virtualMethodA)));
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

  // PolymorphicImplicitCallEdge tests
  @Test
  public void testReflectiveInvokeExample() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample");
    MethodSignature calleeMethodSig =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                            "bachelor.intra.reflection.ReflectiveInvokeExample$Target"),
                    "targetMethod",
                    "void",
                    Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }

  @Test
  public void testReflectiveInvokeExample3() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample3");
    MethodSignature calleeMethodSigA =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType("bachelor.intra.reflection.ReflectiveInvokeExample3$T"),
                    "a",
                    "void",
                    Collections.emptyList());
    MethodSignature calleeMethodSigB =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType("bachelor.intra.reflection.ReflectiveInvokeExample3$T"),
                    "b",
                    "void",
                    Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSigA = cg.callSourcesTo(calleeMethodSigA);
    Set<MethodSignature> calleeSourcesMethodSigB = cg.callSourcesTo(calleeMethodSigB);
    assertTrue(calleeSourcesMethodSigA.contains(mainMethodSignature));
    assertTrue(calleeSourcesMethodSigB.contains(mainMethodSignature));
  }

  @Test
  public void testReflectiveInvokeExample4() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample4");
    MethodSignature calleeMethodSig =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType("bachelor.intra.reflection.ReflectiveInvokeExample4$T"),
                    "helloWorld",
                    "void",
                    Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }

  @Test
  public void testReflectiveInvokeExample6() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample6");
    MethodSignature calleeMethodSig =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType("bachelor.intra.reflection.ReflectiveInvokeExample6$T"),
                    "x",
                    "void",
                    Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }

  @Test
  public void testReflectiveInvokeExample8() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample8");
    MethodSignature calleeMethodSigTarget =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                            "bachelor.intra.reflection.ReflectiveInvokeExample8$Target"),
                    "target",
                    "void",
                    Collections.emptyList());
    MethodSignature calleeMethodSigOther =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                            "bachelor.intra.reflection.ReflectiveInvokeExample8$Target"),
                    "other",
                    "void",
                    Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSigTarget = cg.callSourcesTo(calleeMethodSigTarget);
    Set<MethodSignature> calleeSourcesMethodSigOther = cg.callSourcesTo(calleeMethodSigOther);
    assertTrue(calleeSourcesMethodSigTarget.contains(mainMethodSignature));
    assertTrue(calleeSourcesMethodSigOther.contains(mainMethodSignature));
  }

  @Test
  public void testReflectiveInvokeExample9() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.reflection.ReflectiveInvokeExample9");
    MethodSignature calleeMethodSig1 =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                            "bachelor.intra.reflection.ReflectiveInvokeExample9$Base"),
                    "targetMethod",
                    "void",
                    Collections.emptyList());
    MethodSignature calleeMethodSig2 =
            identifierFactory.getMethodSignature(
                    identifierFactory.getClassType(
                            "bachelor.intra.reflection.ReflectiveInvokeExample9$Base"),
                    "targetMethod",
                    "void",
                    Collections.singletonList("java.lang.String"));
    Set<MethodSignature> calleeSourcesMethodSig1 = cg.callSourcesTo(calleeMethodSig1);
    Set<MethodSignature> calleeSourcesMethodSig2 = cg.callSourcesTo(calleeMethodSig2);
    assertTrue(calleeSourcesMethodSig1.contains(mainMethodSignature));
    assertTrue(calleeSourcesMethodSig2.contains(mainMethodSignature));
  }

  @Test
  public void testCallNewInstance1() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.other.CallNewInstance1");
    MethodSignature calleeMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("bachelor.intra.other.CallNewInstance1$MyClass"),
            "<init>",
            "void",
            Collections.emptyList());
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }

  @Test
  public void testCallNewInstance2() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.other.CallNewInstance2");
    MethodSignature calleeMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("bachelor.intra.other.CallNewInstance2$MyClass"),
            "<init>",
            "void",
            Collections.singletonList("int"));
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }

  @Test
  public void testCallNewInstance3() {
    CallGraph cg = loadCallGraph("Implicit", "bachelor.intra.other.CallNewInstance3");
    List<String> expectedParams = List.of("int", "java.lang.String", "boolean");
    MethodSignature calleeMethodSig =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("bachelor.intra.other.CallNewInstance3$MyClass"),
            "<init>",
            "void",
            expectedParams);
    Set<MethodSignature> calleeSourcesMethodSig = cg.callSourcesTo(calleeMethodSig);
    assertTrue(calleeSourcesMethodSig.contains(mainMethodSignature));
  }
}
