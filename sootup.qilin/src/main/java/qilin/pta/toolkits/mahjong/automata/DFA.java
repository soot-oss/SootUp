package qilin.pta.toolkits.mahjong.automata;

import soot.Type;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author Tian Tan
 * @author Yue Li
 * <p>
 * refactered by Dongjie He.
 */
public class DFA {

    private Set<DFAState> states, allStates;
    private final DFAState q0;

    static final DFAState deadState = new DFAState();

    public DFA(DFAState q0) {
        this.q0 = q0;
    }

    /**
     * @return Set of states. Does not contains dead state.
     */
    public Set<DFAState> getStates() {
        if (states == null) {
            computeStates();
        }
        return states;
    }

    /**
     * @return Set of all states including dead state.
     */
    public Set<DFAState> getAllStates() {
        if (allStates == null) {
            computeStates();
        }
        return allStates;
    }

    private void computeStates() {
        Queue<DFAState> queue = new LinkedList<>();
        queue.add(q0);
        states = new HashSet<>();
        while (!queue.isEmpty()) {
            DFAState s = queue.poll();
            if (!states.contains(s)) {
                states.add(s);
                queue.addAll(s.getNextMap().values());
            }
        }
        allStates = new HashSet<>(states);
        allStates.add(deadState);
    }

    public DFAState getStartState() {
        return q0;
    }

    public static DFAState getDeadState() {
        return deadState;
    }

    public Set<Type> outputOf(DFAState s) {
        return s.getOutput();
    }

}
