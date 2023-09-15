package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.PTA;
import qilin.core.pag.*;
import qilin.pta.toolkits.common.ToolUtil;
import qilin.pta.toolkits.zipper.Global;
import qilin.pta.toolkits.zipper.analysis.PotentialContextElement;
import qilin.util.ANSIColor;
import qilin.util.graph.ConcurrentDirectedGraphImpl;
import qilin.util.graph.Reachability;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

import java.util.*;
import java.util.stream.Collectors;

import static qilin.util.ANSIColor.color;

public class FlowAnalysis {
    private final PTA pta;
    private final PotentialContextElement pce;
    private final ObjectFlowGraph objectFlowGraph;

    private Type currentType;
    private Set<VarNode> inVars;
    private Set<Node> outNodes;
    private Set<Node> visitedNodes;
    private Map<Node, Set<Edge>> wuEdges;
    private ConcurrentDirectedGraphImpl<Node> pollutionFlowGraph;
    private Reachability<Node> reachability;

    public FlowAnalysis(PTA pta,
                        PotentialContextElement pce,
                        ObjectFlowGraph ofg) {
        this.pta = pta;
        this.pce = pce;
        this.objectFlowGraph = ofg;
    }

    public void initialize(Type type, Set<SootMethod> inms, Set<SootMethod> outms) {
        currentType = type;
        inVars = inms.stream()
                .map(m -> ToolUtil.getParameters(pta.getPag(), m))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        outNodes = outms.stream()
                .map(m -> ToolUtil.getRetVars(pta.getPag(), m))
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        visitedNodes = new HashSet<>();
        wuEdges = new HashMap<>();
        pollutionFlowGraph = new ConcurrentDirectedGraphImpl<>();
        reachability = new Reachability<>(pollutionFlowGraph);
    }

    public void analyze(SootMethod startMethod) {
        for (VarNode param : ToolUtil.getParameters(pta.getPag(), startMethod)) {
            if (param != null) {
                dfs(param);
            } else {
                if (Global.isDebug()) {
                    System.out.println(param + " is absent in the flow graph.");
                }
            }
        }

        if (Global.isDebug()) {
            Set<SootMethod> outMethods = new HashSet<>();
            for (VarNode param : ToolUtil.getParameters(pta.getPag(), startMethod)) {
                if (param != null) {
                    for (Node outNode : outNodes) {
                        if (reachability.reachableNodesFrom(param).contains(outNode)) {
                            LocalVarNode outVarNode = (LocalVarNode) outNode;
                            outMethods.add(outVarNode.getMethod());
                        }
                    }
                }
            }
            System.out.println(color(ANSIColor.GREEN, "In method: ") + startMethod);
            System.out.println(color(ANSIColor.GREEN, "Out methods: ") + outMethods);
        }
    }

    public Set<Node> getFlowNodes() {
        Set<Node> results = new HashSet<>();
        for (Node outNode : outNodes) {
            if (pollutionFlowGraph.allNodes().contains(outNode)) {
                results.addAll(reachability.nodesReach(outNode));
            }
        }
        return results;
    }

    public int numberOfPFGNodes() {
        return pollutionFlowGraph.allNodes().size();
    }

    public int numberOfPFGEdges() {
        int nrEdges = 0;
        for (Node node : pollutionFlowGraph.allNodes()) {
            nrEdges += pollutionFlowGraph.succsOf(node).size();
        }
        return nrEdges;
    }

    public ConcurrentDirectedGraphImpl<Node> getPFG() {
        return pollutionFlowGraph;
    }

    public void clear() {
        currentType = null;
        inVars = null;
        outNodes = null;
        visitedNodes = null;
        wuEdges = null;
        pollutionFlowGraph = null;
        reachability = null;
    }

    // a bit more complicated than the algorithm in TOPLAS'20
    private void dfs(Node node) {
        if (Global.isDebug()) {
            System.out.println(color(ANSIColor.BLUE, "Node ") + node);
        }
        if (visitedNodes.contains(node)) { // node has been visited
            if (Global.isDebug()) {
                System.out.println(color(ANSIColor.RED, "Visited node: ") + node);
            }
        } else {
            visitedNodes.add(node);
            pollutionFlowGraph.addNode(node);
            // add unwrapped flow edges
            if (Global.isEnableUnwrappedFlow()) {
                if (node instanceof VarNode var) {
                    Collection<AllocNode> varPts = pta.reachingObjects(var).toCIPointsToSet().toCollection();
                    // Optimization: approximate unwrapped flows to make
                    // Zipper and pointer analysis run faster
                    pta.getCgb().getReceiverToSitesMap()
                            .getOrDefault(var, Collections.emptySet())
                            .forEach(vcs -> {
                                Stmt callsiteStmt = (Stmt) vcs.getUnit();
                                InvokeExpr invo = callsiteStmt.getInvokeExpr();
                                if (!(invo instanceof InstanceInvokeExpr)) {
                                    return;
                                }
                                if (callsiteStmt instanceof AssignStmt assignStmt) {
                                    Value lv = assignStmt.getLeftOp();
                                    if (!(lv.getType() instanceof RefLikeType)) {
                                        return;
                                    }
                                    final VarNode to = (VarNode) pta.getPag().findValNode(lv);
                                    if (outNodes.contains(to)) {
                                        for (VarNode inVar : inVars) {
                                            if (!Collections.disjoint(pta.reachingObjects(inVar).toCIPointsToSet().toCollection(), varPts)) {
                                                Edge unwrappedEdge = new Edge(Kind.UNWRAPPED_FLOW, node, to);
                                                addWUEdge(node, unwrappedEdge);
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                }
            }
            List<Edge> nextEdges = new ArrayList<>();
            for (Edge edge : outEdgesOf(node)) {
                switch (edge.getKind()) {
                    case LOCAL_ASSIGN, UNWRAPPED_FLOW -> {
                        nextEdges.add(edge);
                    }
                    case INTERPROCEDURAL_ASSIGN, INSTANCE_LOAD, WRAPPED_FLOW -> {
                        // next must be a variable
                        LocalVarNode next = (LocalVarNode) edge.getTarget();
                        SootMethod inMethod = next.getMethod();
                        // Optimization: filter out some potential spurious flows due to
                        // the imprecision of context-insensitive pre-analysis, which
                        // helps improve the performance of Zipper and pointer analysis.
                        if (pce.PCEMethodsOf(currentType).contains(inMethod)) {
                            nextEdges.add(edge);
                        }
                    }
                    case INSTANCE_STORE -> {
                        ContextField next = (ContextField) edge.getTarget();
                        AllocNode base = next.getBase();
                        if (base.getType().equals(currentType)) {
                            // add wrapped flow edges to this variable
                            if (Global.isEnableWrappedFlow()) {
                                methodsInvokedOn(currentType).stream()
                                        .map(m -> ToolUtil.getThis(pta.getPag(), m)) // filter this variable of native methods
                                        .map(n -> new Edge(Kind.WRAPPED_FLOW, next, n))
                                        .forEach(e -> addWUEdge(next, e));
                            }
                            nextEdges.add(edge);
                        } else if (pce.allocateesOf(currentType).contains(base)) {
                            // Optimization, similar as above.
                            if (Global.isEnableWrappedFlow()) {
                                Set<VarNode> r = new HashSet<>();
                                AllocNode mBase = (AllocNode) pta.parameterize(base, pta.emptyContext());
                                pta.getPag().allocLookup(mBase).forEach(v -> {
                                    if (v instanceof ContextVarNode cvn) {
                                        if (cvn.base() instanceof LocalVarNode lvn) {
                                            if (!lvn.isThis()) {
                                                r.add(lvn);
                                            }
                                        }
                                    }
                                });
                                Iterator<VarNode> it = r.iterator();
                                if (it.hasNext()) {
                                    Node assigned = r.iterator().next();
                                    if (assigned != null) {
                                        Edge e = new Edge(Kind.WRAPPED_FLOW, next, assigned);
                                        addWUEdge(next, e);
                                    }
                                }
                            }
                            nextEdges.add(edge);
                        }
                    }
                    default -> {
                        throw new RuntimeException("Unknown edge: " + edge);
                    }
                }
            }
            for (Edge nextEdge : nextEdges) {
                Node nextNode = nextEdge.getTarget();
                pollutionFlowGraph.addEdge(node, nextNode);
                dfs(nextNode);
            }
        }
    }

    private void addWUEdge(Node sourceNode, Edge edge) {
        wuEdges.computeIfAbsent(sourceNode, k -> new HashSet<>()).add(edge);
    }

    private Collection<SootMethod> methodsInvokedOn(Type type) {
        return pta.getPag().getAllocNodes()
                .stream().filter(o -> o.getType().equals(type))
                .map(pce::methodsInvokedOn).flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    /**
     * @param node
     * @return out edges of node from OFG, and wuEdges, if present
     */
    private Set<Edge> outEdgesOf(Node node) {
        Set<Edge> outEdges = objectFlowGraph.outEdgesOf(node);
        if (wuEdges.containsKey(node)) {
            outEdges = new HashSet<>(outEdges);
            outEdges.addAll(wuEdges.get(node));
        }
        return outEdges;
    }

    private void outputPollutionFlowGraphSize() {
        int nrNodes = pollutionFlowGraph.allNodes().size();
        int nrEdges = 0;
        for (Node node : pollutionFlowGraph.allNodes()) {
            nrEdges += pollutionFlowGraph.succsOf(node).size();
        }
        System.out.printf("#Size of PFG of %s: %d nodes, %d edges.\n", currentType, nrNodes, nrEdges);
    }
}
