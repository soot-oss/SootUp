package qilin.pta.toolkits.mahjong.automata;

import qilin.util.Pair;
import qilin.util.UnionFindSet;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

import java.util.Collection;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public class DFAEquivalenceChecker {

    /**
     * Check the equivalence of input automata by Hopcroft-Karp algorithm
     * with minor modifications.
     *
     * @param dfa1
     * @param dfa2
     * @return whether dfa1 and dfa2 are equivalent
     */
    public boolean isEquivalent(DFA dfa1, DFA dfa2) {
        CombinedDFA dfa = new CombinedDFA(dfa1, dfa2);
        Set<DFAState> combinedStates = dfa.getStates();
        UnionFindSet<DFAState> uf = new UnionFindSet<>(combinedStates);
        Stack<Pair<DFAState, DFAState>> stack = new Stack<>();

        DFAState s1 = dfa1.getStartState();
        DFAState s2 = dfa2.getStartState();
        uf.union(s1, s2);
        stack.push(new Pair<>(s1, s2));
        while (!stack.isEmpty()) {
            Pair<DFAState, DFAState> pair = stack.pop();
            DFAState q1 = pair.getFirst();
            DFAState q2 = pair.getSecond();
            Stream.concat(dfa.outEdgesOf(q1).stream(), dfa.outEdgesOf(q2).stream()).forEach(field -> {
                DFAState r1 = uf.find(q1.nextState(field));
                DFAState r2 = uf.find(q2.nextState(field));
                if (r1 != r2) {
                    uf.union(r1, r2);
                    stack.push(new Pair<>(r1, r2));
                }
            });
        }
        Collection<Set<DFAState>> mergedStateSets = uf.getDisjointSets();
        return validate(dfa, mergedStateSets);
    }


    /**
     * @param dfa
     * @param mergedStateSets
     * @return true if every state set contains no different output
     * (i.e., types)
     */
    private boolean validate(CombinedDFA dfa, Collection<Set<DFAState>> mergedStateSets) {
        for (Set<DFAState> set : mergedStateSets) {
            int minSize = set.stream()
                    .mapToInt(s -> dfa.outputOf(s).size())
                    .min()
                    .getAsInt();
            long unionSize = set.stream()
                    .flatMap(s -> dfa.outputOf(s).stream())
                    .distinct()
                    .count();
            if (unionSize > minSize) {
                return false;
            }
        }
        return true;
    }


    private static class CombinedDFA {

        DFA dfa1, dfa2;

        private CombinedDFA(DFA dfa1, DFA dfa2) {
            this.dfa1 = dfa1;
            this.dfa2 = dfa2;
        }

        private Set<DFAState> getStates() {
            return Stream
                    .concat(dfa1.getAllStates().stream(), dfa2.getAllStates().stream())
                    .collect(Collectors.toSet());
        }

        private Set<SparkField> outEdgesOf(DFAState s) {
            return s.outEdges();
        }

        private Set<Type> outputOf(DFAState s) {
            return s.getOutput();
        }

    }

}
