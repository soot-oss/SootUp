package qilin.pta.toolkits.mahjong.automata;

import qilin.core.pag.AllocNode;
import qilin.pta.toolkits.common.FieldPointstoGraph;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public class NFA {

    private static final AllocNode deadState = null;

    private final AllocNode q0;
    private final FieldPointstoGraph fpg;

    public NFA(AllocNode q0, FieldPointstoGraph fpg) {
        this.q0 = q0;
        this.fpg = fpg;
    }

    /**
     * This method on-the-fly computes set of states.
     *
     * @return Set of states. Does not contains dead state.
     */
    public Set<AllocNode> getStates() {
        Set<AllocNode> states = new HashSet<>();
        Stack<AllocNode> stack = new Stack<>();
        stack.push(q0);
        while (!stack.isEmpty()) {
            AllocNode s = stack.pop();
            if (!states.contains(s)) {
                states.add(s);
                outEdgesOf(s).forEach(field -> {
                    nextStates(s, field).stream()
                            .filter(obj -> !states.contains(obj))
                            .forEach(stack::push);
                });
            }
        }
        return states;
    }

    public AllocNode getStartState() {
        return q0;
    }

    public AllocNode getDeadState() {
        return deadState;
    }

    public Set<AllocNode> nextStates(AllocNode obj, SparkField f) {
        if (isDeadState(obj) || !fpg.hasFieldPointer(obj, f)) {
            return Collections.singleton(deadState);
        } else {
            return fpg.pointsTo(obj, f);
        }
    }

    public boolean isDeadState(AllocNode obj) {
        return obj == deadState;
    }

    public Set<SparkField> outEdgesOf(AllocNode obj) {
        if (isDeadState(obj)) {
            return Collections.emptySet();
        } else {
            return fpg.outFieldsOf(obj);
        }
    }

    public Type outputOf(AllocNode obj) {
        if (isDeadState(obj)) {
            return null;
        } else {
            return obj.getType();
        }
    }

}
