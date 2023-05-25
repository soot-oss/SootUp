package sootup.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

/** @author Kadiray Karakaya, Jonas Klauke */
@Category(Java8Test.class)
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

    MethodSignature clinitObject =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("java.lang.Object"),
            "<clinit>",
            "void",
            Collections.emptyList());

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

    assertFalse(cg.containsMethod(virtualMethodA));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodB));
    assertFalse(cg.containsMethod(virtualMethodC));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodD));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodE));

    assertTrue(cg.containsCall(mainMethodSignature, clinitObject));

    assertEquals(8, cg.callsFrom(mainMethodSignature).size());

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

    assertEquals(
        cg.toString().replace("\n", "").replace("\t", ""),
        "GraphBasedCallGraph(15):"
            + "<example1.A: void <init>()>:"
            + "to <java.lang.Object: void <init>()>"
            + "from <example1.B: void <init>()>"
            + "from <example1.D: void <init>()>"
            + "from <example1.E: void <init>()>"
            + ""
            + "<example1.B: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.B: void staticDispatch(java.lang.Object)>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.B: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.C: void <init>()>:"
            + "to <example1.D: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.D: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.C: void <init>()>"
            + ""
            + "<example1.D: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.E: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.E: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + ""
            + "<example1.Example: void main(java.lang.String[])>:"
            + "to <example1.B: void <init>()>"
            + "to <example1.B: void staticDispatch(java.lang.Object)>"
            + "to <example1.B: void virtualDispatch()>"
            + "to <example1.C: void <init>()>"
            + "to <example1.D: void virtualDispatch()>"
            + "to <example1.E: void <init>()>"
            + "to <example1.E: void virtualDispatch()>"
            + "to <java.lang.Object: void <clinit>()>"
            + ""
            + "<java.lang.Object: void <clinit>()>:"
            + "to <java.lang.Object: void <clinit>()>"
            + "to <java.lang.Object: void registerNatives()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "from <java.lang.Object: void <clinit>()>"
            + ""
            + "<java.lang.Object: void <init>()>:"
            + "from <example1.A: void <init>()>"
            + ""
            + "<java.lang.Object: void registerNatives()>:"
            + "from <java.lang.Object: void <clinit>()>");
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

    assertFalse(cg.containsCall(alreadyVisitedMethod, newTargetA));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetB));
    assertTrue(cg.containsCall(alreadyVisitedMethod, newTargetC));
  }

  @Test
  public void testInstantiatedClassInClinit() {
    CallGraph cg = loadCallGraph("RTA", false, "cic.Class");
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
    assertTrue(cg.containsCall(mainMethodSignature, instantiatedClassMethod));
    assertFalse(cg.containsCall(mainMethodSignature, nonInstantiatedClassMethod));
  }

  @Test
  public void testLaterInstantiatedClass() {
    CallGraph cg = loadCallGraph("RTA", false, "lic.Class");
    MethodSignature instantiatedClassMethod =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("lic.InstantiatedClass"),
            "method",
            "void",
            Collections.emptyList());
    assertTrue(cg.containsCall(mainMethodSignature, instantiatedClassMethod));
  }
}
