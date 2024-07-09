package sootup.callgraph;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import sootup.core.signatures.MethodSignature;

import java.util.*;

public class CallGraphDifference {

    private MethodSignature entrypoint;

    List<Pair<MethodSignature, MethodSignature>> cg1Edges;
    List<Pair<MethodSignature, MethodSignature>> cg2Edges;

    public CallGraphDifference(CallGraph cg1, CallGraph cg2, MethodSignature entrypoint) {
        this.cg1Edges = constructEdges(cg1);
        this.cg2Edges = constructEdges(cg2);
    }

    public CallGraphDifference(CallGraph cg1, CallGraph cg2) {
        this.cg1Edges = constructEdges(cg1);
        this.cg2Edges = constructEdges(cg2);
    }

    private List<Pair<MethodSignature, MethodSignature>> constructEdges(CallGraph cg) {
        List<Pair<MethodSignature, MethodSignature>> cgEdges = new ArrayList<>();
        for (MethodSignature srcNode : cg.getMethodSignatures()) {
            Set<MethodSignature> outNodes = cg.callsFrom(srcNode);
            for (MethodSignature targetNode : outNodes) {
                cgEdges.add(new MutablePair<>(srcNode, targetNode));
            }
        }
        return cgEdges;
    }

    /*
    In the addedEdges() function, we iterate over each edge in cg2Edges and
    check if it exists in cg1Edges. If it doesn't, we add it to the addedEdges list.
    */
    public List<Pair<MethodSignature, MethodSignature>> addedEdges() {
        List<Pair<MethodSignature, MethodSignature>> addedEdges = new ArrayList<>();
        for (Pair<MethodSignature, MethodSignature> edge : cg2Edges) {
            if (!cg1Edges.contains(edge)) {
                addedEdges.add(edge);
            }
        }
        return addedEdges;
    }

    /*
    In the removedEdges() function, we iterate over each edge in cg1Edges and
    check if it exists in cg2Edges. If it doesn't, we add it to the removedEdges list.
    */
    public List<Pair<MethodSignature, MethodSignature>> removedEdges() {
        List<Pair<MethodSignature, MethodSignature>> removedEdges = new ArrayList<>();
        for (Pair<MethodSignature, MethodSignature> edge : cg1Edges) {
            if (!cg2Edges.contains(edge)) {
                removedEdges.add(edge);
            }
        }
        return removedEdges;
    }

}
