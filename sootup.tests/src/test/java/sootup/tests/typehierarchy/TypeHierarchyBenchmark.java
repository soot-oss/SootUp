package sootup.tests.typehierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * Manual micro-benchmark (not run in CI) comparing {@code ViewTypeHierarchy} build/query cost. Run
 * with: mvn -pl sootup.tests -am test -Dtest=TypeHierarchyBenchmark#benchmark -DfailIfNoTests=false
 */
public class TypeHierarchyBenchmark {

  @Test
  @Disabled("manual benchmark, not part of CI")
  public void benchmark() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    View view = new JavaView(inputLocations);

    long t0 = System.nanoTime();
    List<SootClass> classes = view.getClasses().collect(java.util.stream.Collectors.toList());
    long t1 = System.nanoTime();
    System.out.printf("class loading: %d classes in %.1f ms%n", classes.size(), (t1 - t0) / 1e6);

    List<ClassType> types = new ArrayList<>();
    for (SootClass c : classes) {
      types.add(c.getType());
    }

    // --- 1) hierarchy build (first structural query forces it) ---
    long b0 = System.nanoTime();
    long total = view.getTypeHierarchy().subtypesOf(types.get(0)).count();
    long b1 = System.nanoTime();
    System.out.printf(
        "hierarchy build (first subtypesOf call): %.1f ms (result size %d)%n",
        (b1 - b0) / 1e6, total);

    Random rnd = new Random(42);
    int n = types.size();

    // --- 2) subtypesOf over every class ---
    long s0 = System.nanoTime();
    long sum = 0;
    for (ClassType t : types) {
      sum += view.getTypeHierarchy().subtypesOf(t).count();
    }
    long s1 = System.nanoTime();
    System.out.printf(
        "subtypesOf all %d classes: %.1f ms (total subtype entries %d)%n", n, (s1 - s0) / 1e6, sum);

    // --- 3) isSubtype random pairs ---
    int pairs = 500_000;
    List<int[]> idx = new ArrayList<>(pairs);
    for (int i = 0; i < pairs; i++) {
      idx.add(new int[] {rnd.nextInt(n), rnd.nextInt(n)});
    }
    long i0 = System.nanoTime();
    int trueCount = 0;
    for (int[] p : idx) {
      if (view.getTypeHierarchy().isSubtype(types.get(p[0]), types.get(p[1]))) {
        trueCount++;
      }
    }
    long i1 = System.nanoTime();
    System.out.printf(
        "isSubtype %d random pairs: %.1f ms (%d true)%n", pairs, (i1 - i0) / 1e6, trueCount);

    // --- 4) LCA random pairs ---
    int lcaPairs = 50_000;
    List<int[]> lcaIdx = new ArrayList<>(lcaPairs);
    for (int i = 0; i < lcaPairs; i++) {
      lcaIdx.add(new int[] {rnd.nextInt(n), rnd.nextInt(n)});
    }
    long l0 = System.nanoTime();
    long lcaSum = 0;
    for (int[] p : lcaIdx) {
      lcaSum +=
          view.getTypeHierarchy().getLowestCommonAncestors(types.get(p[0]), types.get(p[1])).size();
    }
    long l1 = System.nanoTime();
    System.out.printf(
        "getLowestCommonAncestors %d random pairs: %.1f ms (sum sizes %d)%n",
        lcaPairs, (l1 - l0) / 1e6, lcaSum);

    Collections.shuffle(types, rnd);
  }
}
