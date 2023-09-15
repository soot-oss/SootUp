package qilin.pta.toolkits.mahjong.automata;

import qilin.core.pag.AllocNode;
import soot.Type;
import soot.jimple.spark.pag.SparkField;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tian Tan
 * @author Yue Li
 */
public class DFAState {

    private final Set<AllocNode> objs;
    private final Set<Type> output;
    private final Map<SparkField, DFAState> nextMap;

    private boolean hasHashCode = false;
    private int hashCode;

    public DFAState() {
        this.objs = Collections.emptySet();
        this.output = Collections.emptySet();
        this.nextMap = new HashMap<>();
    }

    public DFAState(Set<AllocNode> objs, Set<Type> output) {
        this.objs = objs;
        this.output = output;
        this.nextMap = new HashMap<>();
    }

    public Set<AllocNode> getObjects() {
        return objs;
    }

    public Set<Type> getOutput() {
        return output;
    }

    void addTransition(SparkField f, DFAState nextState) {
        nextMap.put(f, nextState);
    }

    Map<SparkField, DFAState> getNextMap() {
        return nextMap;
    }

    DFAState nextState(SparkField f) {
        return nextMap.getOrDefault(f, DFA.getDeadState());
    }

    Set<SparkField> outEdges() {
        return nextMap.keySet();
    }

    /**
     * Cache hash code.
     */
    @Override
    public int hashCode() {
        if (!hasHashCode) {
            hashCode = objs.hashCode();
            hasHashCode = true;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DFAState anoDFAState)) {
            return false;
        }
        return getObjects().equals(anoDFAState.getObjects());
    }

    @Override
    public String toString() {
        return getObjects().stream()
                .map(obj -> obj == null ? "null" : String.valueOf(obj.getNumber()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .toString();
    }

}
