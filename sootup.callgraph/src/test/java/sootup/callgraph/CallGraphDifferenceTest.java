package sootup.callgraph;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class CallGraphDifferenceTest {

  @Test
  public void testCGDiff() {
    String baseDir = "src/test/resources/callgraph/CallGraphDifference/binary/";
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            baseDir, SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(inputLocation);

    ClassHierarchyAnalysisAlgorithm chaAlgorithm = new ClassHierarchyAnalysisAlgorithm(view);
    JavaClassType chaClassType = view.getIdentifierFactory().getClassType("Example");
    MethodSignature chaMethodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(
                chaClassType, "main", "void", Collections.singletonList("java.lang.String[]"));
    CallGraph cg1 = chaAlgorithm.initialize(Collections.singletonList(chaMethodSignature));

    RapidTypeAnalysisAlgorithm rtaAlgorithm = new RapidTypeAnalysisAlgorithm(view);
    CallGraph cg2 = rtaAlgorithm.initialize(Collections.singletonList(chaMethodSignature));

    CallGraphDifference callGraphDifference = cg1.diff(cg2);
    System.out.println("Unique Base Graph Calls/Edges");
    List<Pair<MethodSignature, MethodSignature>> uniqueBaseGraphCalls =
        callGraphDifference.uniqueBaseGraphCalls();
    uniqueBaseGraphCalls.forEach(System.out::println);
    System.out.println("Unique Base Graph Methods/Nodes");
    List<MethodSignature> uniqueBaseGraphMethods = callGraphDifference.uniqueBaseGraphMethods();
    uniqueBaseGraphMethods.forEach(System.out::println);

    System.out.println("Unique Other Graph Calls/Edges");
    List<Pair<MethodSignature, MethodSignature>> uniqueOtherGraphCalls =
        callGraphDifference.uniqueOtherGraphCalls();
    uniqueOtherGraphCalls.forEach(System.out::println);
    System.out.println("Unique Other Graph Methods/Nodes");
    List<MethodSignature> uniqueOtherGraphMethods = callGraphDifference.uniqueOtherGraphMethods();
    uniqueOtherGraphMethods.forEach(System.out::println);

    System.out.println("Intersected Calls/Edges");
    List<Pair<MethodSignature, MethodSignature>> intersectedCalls =
        callGraphDifference.intersectedCalls();
    intersectedCalls.forEach(System.out::println);
    System.out.println("Intersected Methods/Nodes");
    List<MethodSignature> intersectedMethods = callGraphDifference.intersectedMethods();
    intersectedMethods.forEach(System.out::println);
  }
}
