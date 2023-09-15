package qilin.pta.toolkits.debloaterx;

import qilin.core.pag.LocalVarNode;
import qilin.core.pag.Node;
import qilin.util.Pair;
import qilin.util.queue.UniqueQueue;
import soot.SootMethod;
import soot.jimple.spark.pag.SparkField;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InterFlowAnalysis {
    protected final XUtility utility;
    protected final XPAG xpag;
    /* records the reachability info: given a field F, (1) which params could be saved into F
     * (2) F could flows to which method's param or return node.
     */
    protected final Map<SparkField, Set<LocalVarNode>> field2InParams = new ConcurrentHashMap<>();
    protected final Map<SparkField, Set<LocalVarNode>> field2OutParams = new ConcurrentHashMap<>();

    public InterFlowAnalysis(XUtility utility) {
        this.utility = utility;
        this.xpag = utility.getXpag();
        reachabilityAnalysis();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* reachability analysis: fill content in field2InParams and field2Outs
     * This is the implementation of Algorithm 2 in the paper.
     * */
    public void reachabilityAnalysis() {
        Set<SparkField> fields = utility.getFields();
        fields.parallelStream().forEach(field -> {
            // compute the value of field could be loaded to which outmethods.
            Set<LocalVarNode> retOrParams = runDFAandCollect(field, false);
            if (!retOrParams.isEmpty()) {
                field2OutParams.computeIfAbsent(field, k -> ConcurrentHashMap.newKeySet()).addAll(retOrParams);
            }
            // compute the params could be stored into the specified field.
            Set<LocalVarNode> params = runDFAandCollect(field, true);
            if (!params.isEmpty()) {
                field2InParams.computeIfAbsent(field, k -> ConcurrentHashMap.newKeySet()).addAll(params);
            }
        });
    }

    /*
     * When isInflow = true, check whether the value in param could be stored into field.
     * Otherwise, check whether the value in field could be loaded to any method's return value or params.
     * */
    private Set<LocalVarNode> runDFAandCollect(SparkField field, boolean isInflow) {
        Set<LocalVarNode> ret = new HashSet<>();
        Map<State, Set<Node>> state2nodes = new HashMap<>();
        Queue<Pair<Node, State>> queue = new UniqueQueue<>();
        queue.add(new Pair<>(xpag.getDummyThis(), State.THIS));
        while (!queue.isEmpty()) {
            Pair<Node, State> front = queue.poll();
            if (front.getSecond() == State.End) {
                if (front.getFirst() instanceof LocalVarNode lvn) {
                    ret.add(lvn);
                }
            }
            // visit the node and state.
            visit(front, state2nodes);
            Set<Pair<Node, State>> nexts = getNextNodeStates(front, field, isInflow);
            for (Pair<Node, State> nodeState : nexts) {
                if (!isVisited(nodeState, state2nodes)) {
                    queue.add(nodeState);
                }
            }
        }
        return ret;
    }

    private Set<Pair<Node, State>> getNextNodeStates(Pair<Node, State> nodeState, SparkField field, boolean in) {
        Node node = nodeState.getFirst();
        State state = nodeState.getSecond();
        Set<Pair<Node, State>> ret = new HashSet<>();
        for (Edge edge : xpag.getOutEdges(node)) {
            boolean mathched = edge.field != null && edge.field.equals(field);
            State nextState;
            if (in) {
                nextState = nextStateForIn(state, edge.kind, mathched);
            } else {
                nextState = nextStateForOut(state, edge.kind, mathched);
            }
            if (nextState != State.Error) {
                ret.add(new Pair<>(edge.to, nextState));
            }
        }
        return ret;
    }

    /*
     * This method describes another automaton and its transition function for checking whether the field value could be
     * loaded to any method's return.
     * Correspoing to the automaton given in Fig 7c in the paper.
     * */
    private State nextStateForOut(State currState, EdgeKind kind, boolean fieldMatch) {
        switch (currState) {
            case THIS -> {
                if (kind == EdgeKind.ITHIS) {
                    return State.ThisAlias;
                }
            }
            case ThisAlias -> {
                if (kind == EdgeKind.ASSIGN) {
                    return State.ThisAlias;
                } else if (kind == EdgeKind.LOAD && fieldMatch) {
                    return State.VPlus;
                }
            }
            case VPlus -> {
                if (kind == EdgeKind.ASSIGN || kind == EdgeKind.LOAD || kind == EdgeKind.CLOAD) {
                    return State.VPlus;
                } else if (kind == EdgeKind.RETURN) {
                    return State.End;
                } else if (kind == EdgeKind.CSTORE || kind == EdgeKind.STORE || kind == EdgeKind.ICSTORE || kind == EdgeKind.ISTORE) {
                    //kind == EdgeKind.ICSTORE || kind == EdgeKind.ISTORE can be removed.
                    return State.VMinus;
                }
            }
            case VMinus -> {
                if (kind == EdgeKind.IASSIGN || kind == EdgeKind.ILOAD || kind == EdgeKind.ICLOAD) {
                    return State.VMinus;
                } else if (kind == EdgeKind.INEW) {
                    return State.O;
                } else if (kind == EdgeKind.PARAM) {
                    return State.End;
                }
            }
            case O -> {
                if (kind == EdgeKind.NEW) {
                    return State.VPlus;
                }
            }
        }
        return State.Error;
    }

    private void visit(Pair<Node, State> nodeState, Map<State, Set<Node>> state2nodes) {
        Node node = nodeState.getFirst();
        State state = nodeState.getSecond();
        state2nodes.computeIfAbsent(state, k -> new HashSet<>()).add(node);
    }

    private boolean isVisited(Pair<Node, State> nodeState, Map<State, Set<Node>> state2nodes) {
        Node node = nodeState.getFirst();
        State state = nodeState.getSecond();
        Set<Node> nodes = state2nodes.getOrDefault(state, Collections.emptySet());
        return nodes.contains(node);
    }

    /*
     * This method describes an automaton and its transition function for checking whether the value could be stored
     * into any field.
     * Correspoing to the automaton given in Fig 7b in the paper.
     * */
    private State nextStateForIn(State currState, EdgeKind kind, boolean fieldMatch) {
        switch (currState) {
            case O -> {
                if (kind == EdgeKind.NEW) {
                    return State.VPlus;
                }
            }
            case THIS -> {
                if (kind == EdgeKind.ITHIS) {
                    return State.ThisAlias;
                }
            }
            case VPlus -> {
                if (kind == EdgeKind.ASSIGN || kind == EdgeKind.LOAD || kind == EdgeKind.CLOAD) {
                    return State.VPlus;
                } else if (kind == EdgeKind.ISTORE || kind == EdgeKind.STORE || kind == EdgeKind.ICSTORE || kind == EdgeKind.CSTORE) {
                    // kind == EdgeKind.STORE || kind == EdgeKind.ICSTORE can be deleted due to not occured in the real world.
                    return State.VMinus;
                }
            }
            case VMinus -> {
                if (kind == EdgeKind.IASSIGN || kind == EdgeKind.ILOAD || kind == EdgeKind.ICLOAD) {
                    return State.VMinus;
                } else if (kind == EdgeKind.INEW) {
                    return State.O;
                } else if (kind == EdgeKind.PARAM) {
                    return State.End;
                }
            }
            case ThisAlias -> {
                if (kind == EdgeKind.ASSIGN) {
                    return State.ThisAlias;
                } else if (kind == EdgeKind.ISTORE) {
                    if (fieldMatch) {
                        return State.VMinus;
                    }
                } else if (kind == EdgeKind.LOAD) {
                    if (fieldMatch) {
                        return State.VPlus;
                    }
                }
            }
        }
        return State.Error;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* APIs for query */
    public Set<LocalVarNode> getParamsStoredInto(SparkField field) {
        return this.field2InParams.getOrDefault(field, Collections.emptySet());
    }

    public Set<SootMethod> getOutMethodsWithRetOrParamValueFrom(SparkField field) {
        return field2OutParams.getOrDefault(field, Collections.emptySet()).stream().map(LocalVarNode::getMethod).collect(Collectors.toSet());
    }

}
