package de.upb.swt.soot.test.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.algorithm.RapidTypeAnalysisAlgorithm;
import de.upb.swt.soot.callgraph.model.CallGraph;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kadiray Karakaya */
@Category(Java8Test.class)
public class RapidTypeAnalysisAlgorithmTest extends CallGraphTestBase<RapidTypeAnalysisAlgorithm> {

  @Override
  protected RapidTypeAnalysisAlgorithm createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    return new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
  }

  @Test
  public void testMiscExample1() {
    /** We expect constructors for B and C We expect A.print(), B.print(), C.print() */
    CallGraph cg = loadCallGraph("Misc", "example1.Example");

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            "<init>",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.A"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.B"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.C"),
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            "print",
            identifierFactory.getClassType("example1.D"),
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertFalse(cg.containsMethod(methodD));

    assertEquals(5, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
  }
}
