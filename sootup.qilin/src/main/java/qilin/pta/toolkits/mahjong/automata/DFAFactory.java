package qilin.pta.toolkits.mahjong.automata;

import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.common.FieldPointstoGraph;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public class DFAFactory {

    private final FieldPointstoGraph fpg;

    private Map<Set<AllocNode>, DFAState> stateMap;
    private Set<DFAState> states, visited;

    public DFAFactory(FieldPointstoGraph fpg) {
        this.fpg = fpg;
        buildAllDFA();
    }

    public DFA getDFA(AllocNode obj) {
        DFAState q0 = stateMap.get(Collections.singleton(obj));
        return new DFA(q0);
    }

    private void buildAllDFA() {
        stateMap = new HashMap<>();
        states = new HashSet<>();
        visited = new HashSet<>();
        fpg.getAllObjs().forEach(this::buildDFA);
    }

    /**
     * Perform subset construction algorithm to convert an NFA
     * to a DFA. If a set of NFA states are merged to an existing
     * DFA state, then reused the existing DFA state instead of creating
     * an equivalent new one.
     *
     * @param obj the start state (object) of the DFA
     */
    private void buildDFA(AllocNode obj) {
        Set<AllocNode> q0Set = Collections.singleton(obj);
        if (!stateMap.containsKey(q0Set)) {
            NFA nfa = new NFA(obj, fpg);
            DFAState startState = getDFAState(q0Set, nfa);
            Queue<DFAState> worklist = new LinkedList<>();
            states.add(startState);
            worklist.add(startState);
            while (!worklist.isEmpty()) {
                DFAState s = worklist.poll();
                if (!visited.contains(s)) {
                    visited.add(s);
                    Set<SparkField> fields = fields(nfa, s.getObjects());
                    fields.forEach(f -> {
                        Set<AllocNode> nextNFAStates = move(nfa, s.getObjects(), f);
                        DFAState nextState = getDFAState(nextNFAStates, nfa);
                        if (!states.contains(nextState)) {
                            states.add(nextState);
                            worklist.add(nextState);
                        }
                        addTransition(s, f, nextState);
                    });
                }
            }
        }
    }

    private DFAState getDFAState(Set<AllocNode> objs, NFA nfa) {
        if (!stateMap.containsKey(objs)) {
            Set<Type> output = objs.stream()
                    .map(nfa::outputOf)
                    .collect(Collectors.toSet());
            stateMap.put(objs, new DFAState(objs, output));
        }
        return stateMap.get(objs);
    }

    private Set<AllocNode> move(NFA nfa, Set<AllocNode> objs, SparkField f) {
        return objs.stream()
                .map(obj -> nfa.nextStates(obj, f))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<SparkField> fields(NFA nfa, Set<AllocNode> objs) {
        return objs.stream()
                .map(nfa::outEdgesOf)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private void addTransition(DFAState s, SparkField f, DFAState nextState) {
        s.addTransition(f, nextState);
    }

}
