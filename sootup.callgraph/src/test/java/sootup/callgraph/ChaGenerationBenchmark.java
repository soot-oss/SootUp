package sootup.callgraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * Manual micro-benchmark (not run in CI) for whole-CHA call-graph generation cost against the
 * {@code TypeHierarchy} implementation, using the same {@code CHAPerf} fixture ({@code
 * Object.toString()} dispatched over the full JDK runtime) as {@link
 * ClassHierarchyAnalysisAlgorithmTest#testRepeatedResolveCallIsCheapAfterFirstResolution()}. Run
 * with: mvn -pl sootup.callgraph -am test -Dtest=ChaGenerationBenchmark#benchmark
 * -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false
 */
public class ChaGenerationBenchmark {

  @Test
  public void benchmark() throws Exception {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation("src/test/resources/callgraph/CHAPerf/binary"));
    JavaView view = new JavaView(inputLocations);
    JavaIdentifierFactory idf = view.getIdentifierFactory();

    MethodSignature mainSig =
        idf.getMethodSignature(idf.getClassType("chaperf.Example"), idf.getMainSubSignature());

    // --- 1) full CHA call graph construction from a single entry point ---
    long g0 = System.nanoTime();
    ClassHierarchyAnalysisAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainSig));
    long g1 = System.nanoTime();
    System.out.printf(
        "CHA initialize(): %.1f ms (methods=%d, calls=%d)%n",
        (g1 - g0) / 1e6, cg.getMethodSignatures().size(), cg.callCount());

    // --- 2) cold resolveCall for Object.toString(), dispatched over the whole JDK runtime ---
    JavaView freshView =
        new JavaView(
            List.of(
                new DefaultRuntimeAnalysisInputLocation(),
                new JavaClassPathAnalysisInputLocation(
                    "src/test/resources/callgraph/CHAPerf/binary")));
    SootMethod mainMethod = freshView.getMethod(mainSig).orElseThrow();
    InvokableStmt toStringInvoke = null;
    for (Stmt stmt : mainMethod.getBody().getStmts()) {
      if (stmt instanceof InvokableStmt) {
        Optional<AbstractInvokeExpr> expr = ((InvokableStmt) stmt).getInvokeExpr();
        if (expr.isPresent() && !(expr.get() instanceof JSpecialInvokeExpr)) {
          toStringInvoke = (InvokableStmt) stmt;
          break;
        }
      }
    }
    if (toStringInvoke == null) {
      throw new IllegalStateException("expected to find the Object.toString() call site");
    }
    ClassHierarchyAnalysisAlgorithm perfAlgorithm = new ClassHierarchyAnalysisAlgorithm(freshView);

    long c0 = System.nanoTime();
    long targets = perfAlgorithm.resolveCall(mainMethod, toStringInvoke).count();
    long c1 = System.nanoTime();
    System.out.printf(
        "cold resolveCall(Object.toString()): %.1f ms (%d targets)%n", (c1 - c0) / 1e6, targets);

    long w0 = System.nanoTime();
    for (int i = 0; i < 500; i++) {
      perfAlgorithm.resolveCall(mainMethod, toStringInvoke).collect(Collectors.toList());
    }
    long w1 = System.nanoTime();
    System.out.printf(
        "warm resolveCall x500: %.1f ms total (%.3f ms/call)%n",
        (w1 - w0) / 1e6, (w1 - w0) / 1e6 / 500);
  }
}
