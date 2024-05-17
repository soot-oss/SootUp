package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

/**
 * Input source examples taken from <a href="https://bitbucket.org/delors/cats/src/master/">cats</a>
 *
 * @author Markus Schmidt
 */
@Tag("Java8")
public class ClassHierarchyAnalysisAlgorithmTest
    extends CallGraphTestBase<ClassHierarchyAnalysisAlgorithm> {

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

    MethodSignature clinitObject =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("java.lang.Object"),
            "<clinit>",
            "void",
            Collections.emptyList());

    assertFalse(cg.containsCall(mainMethodSignature, constructorA, getInvokableStmt(mainMethodSignature, constructorA)));
    assertTrue(cg.containsCall(mainMethodSignature, constructorB, getInvokableStmt(mainMethodSignature, constructorB)));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC, getInvokableStmt(mainMethodSignature, constructorC)));
    assertFalse(cg.containsCall(mainMethodSignature, constructorD, getInvokableStmt(mainMethodSignature, constructorD)));
    assertTrue(cg.containsCall(mainMethodSignature, constructorE, getInvokableStmt(mainMethodSignature, constructorE)));

    assertFalse(cg.containsMethod(staticMethodA));
    assertTrue(cg.containsCall(mainMethodSignature, staticMethodB,getInvokableStmt(mainMethodSignature, staticMethodB)));
    assertFalse(cg.containsMethod(staticMethodC));
    assertFalse(cg.containsMethod(staticMethodD));
    assertFalse(cg.containsMethod(staticMethodE));

    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodA, getInvokableStmt(mainMethodSignature, virtualMethodA)));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodB, getInvokableStmt(mainMethodSignature, virtualMethodB)));
    assertFalse(cg.containsMethod(virtualMethodC));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodD, getInvokableStmt(mainMethodSignature, virtualMethodD)));
    assertTrue(cg.containsCall(mainMethodSignature, virtualMethodE, getInvokableStmt(mainMethodSignature, virtualMethodE)));

    assertTrue(cg.containsCall(mainMethodSignature, clinitObject, getInvokableStmt(mainMethodSignature, clinitObject)));

    assertEquals(9, cg.callsFrom(mainMethodSignature).size());

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

    assertEquals(
        cg.toString().replace("\n", "").replace("\t", ""),
        "GraphBasedCallGraph(14):"
            + "<example1.A: void <init>()>:"
            + "to <java.lang.Object: void <init>()>"
            + "from <example1.B: void <init>()>"
            + "from <example1.D: void <init>()>"
            + "from <example1.E: void <init>()>"
            + "<example1.A: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.B: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.B: void staticDispatch(java.lang.Object)>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.B: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.C: void <init>()>:"
            + "to <example1.D: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.D: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.C: void <init>()>"
            + "<example1.D: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.E: void <init>()>:"
            + "to <example1.A: void <init>()>"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.E: void virtualDispatch()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<example1.Example: void main(java.lang.String[])>:"
            + "to <example1.A: void virtualDispatch()>"
            + "to <example1.B: void <init>()>"
            + "to <example1.B: void staticDispatch(java.lang.Object)>"
            + "to <example1.B: void virtualDispatch()>"
            + "to <example1.C: void <init>()>"
            + "to <example1.D: void virtualDispatch()>"
            + "to <example1.E: void <init>()>"
            + "to <example1.E: void virtualDispatch()>"
            + "to <java.lang.Object: void <clinit>()>"
            + "<java.lang.Object: void <clinit>()>:"
            + "from <example1.Example: void main(java.lang.String[])>"
            + "<java.lang.Object: void <init>()>:"
            + "from <example1.A: void <init>()>");
  }
}
