package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import sootup.callgraph.mock.PruningCallGraphScope;
import sootup.core.IdentifierFactory;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class ClassHierarchyAnalysisPruningAlgorithmTest extends CallGraphTest {

  protected ClassHierarchyAnalysisAlgorithm createAlgorithm(JavaView view) {
    return new ClassHierarchyAnalysisAlgorithm(view, new PruningCallGraphScope(view));
  }

  /**
   * In this test case, the call graph of Pruning from the folder {@link
   * callgraph.Misc.binary.prune} is created using the <code>ClassHierarchyAnalysisAlgorithm</code>
   * with a {@link PruningCallGraphScope}. It is expected that <code>methodB()</code> is not
   * included as a caller in the call graph, because the scope excludes calls originating from
   * <code>methodB</code>.
   */
  @Test
  public void testFalsePruningCHA() {
    // CHA with a CallGraphScope excluding methodB() and all calls from methodB()
    JavaView view = createViewForClassPath("src/test/resources/callgraph/Misc/binary");
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaClassType mainClassSignature =
        (JavaClassType) identifierFactory.getClassType("prune.Pruning");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, identifierFactory.getMainSubSignature());
    SootClass sc = view.getClass(mainClassSignature).orElse(null);
    assertNotNull(sc);
    SootMethod m = sc.getMethod(mainMethodSignature.getSubSignature()).orElse(null);
    assertNotNull(m, mainMethodSignature + " not found in classloader");
    CallGraph cg = createAlgorithm(view).initialize(Collections.singletonList(mainMethodSignature));
    assertNotNull(cg);
    assertTrue(
        cg.containsMethod(mainMethodSignature), mainMethodSignature + " is not found in CallGraph");
    MethodSignature pruned =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("prune.Pruning"),
            "methodB",
            "void",
            Collections.emptyList());
    MethodSignature neverCalled =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("prune.Pruning"),
            "methodC",
            "void",
            Collections.emptyList());
    assertEquals(0, cg.callsFrom(pruned).size());
    assertFalse(cg.containsMethod(neverCalled));
  }
}
