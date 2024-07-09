package sootup.callgraph;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.views.JavaView;

import java.util.List;

public class CallGraphDifferenceTest extends CallGraphTestBase {

    @Test
    public void testCGDiff() {
        CallGraph cg1 = loadCallGraph("CallGraphDifference", true, "cg1.CG1");
        CallGraph cg2 = loadCallGraph("CallGraphDifference", true, "cg2.CG2");
        CallGraphDifference callGraphDifference = new CallGraphDifference(cg1, cg2);
        List<Pair<MethodSignature, MethodSignature>> addedEdges = callGraphDifference.addedEdges();
        addedEdges.forEach(System.out::println);
        List<Pair<MethodSignature, MethodSignature>> removedEdges = callGraphDifference.removedEdges();
        removedEdges.forEach(System.out::println);
    }

    @Override
    protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view) {
        return new ClassHierarchyAnalysisAlgorithm(view);
    }
}
