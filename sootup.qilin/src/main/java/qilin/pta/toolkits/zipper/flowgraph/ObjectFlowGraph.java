package qilin.pta.toolkits.zipper.flowgraph;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.pta.toolkits.zipper.Global;
import soot.SootMethod;
import soot.Value;
import soot.jimple.*;

import java.util.*;

public class ObjectFlowGraph implements IObjectFlowGraph {
    private final PTA pta;
    private Map<Node, Set<Edge>> outEdges;

    public ObjectFlowGraph(PTA pta) {
        this.pta = pta;
        init();
    }

    @Override
    public Set<Edge> outEdgesOf(Node node) {
        return outEdges.getOrDefault(node, Collections.emptySet());
    }

    @Override
    public Set<Node> allNodes() {
        return outEdges.keySet();
    }

    private boolean localVarBase(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            return cvn.base() instanceof LocalVarNode;
        } else {
            return valNode instanceof LocalVarNode;
        }
    }

    private LocalVarNode fetchLocalVar(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            if (cvn.base() instanceof LocalVarNode) {
                return (LocalVarNode) cvn.base();
            }
        } else if (valNode instanceof LocalVarNode) {
            return (LocalVarNode) valNode;
        }
        return null;
    }

    private LocalVarNode fetchVar(ValNode valNode) {
        if (valNode instanceof ContextVarNode cvn) {
            VarNode base = cvn.base();
            if (base instanceof LocalVarNode lvn) {
                return lvn;
            }
        } else if (valNode instanceof LocalVarNode lvn) {
            return lvn;
        }
        return null;
    }

    public void addOutEdge(final Edge e) {
        outEdges.computeIfAbsent(e.getSource(), k -> new HashSet<>()).add(e);
    }

    private void init() {
        outEdges = new HashMap<>();

        pta.getPag().getSimple().forEach((s, ts) -> {
            if (localVarBase(s)) {
                ts.forEach(t -> {
                    if (localVarBase(t)) {
                        LocalVarNode toNode = fetchVar(t);
                        LocalVarNode fromNode = fetchVar(s);
                        if (fetchLocalVar(s).isInterProcSource()) {
                            addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, toNode));
                        } else {
                            if (fetchLocalVar(t).isInterProcTarget()) {
                                if (!fetchLocalVar(t).isThis()) {
                                    addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, toNode));
                                }
                            } else {
                                addOutEdge(new Edge(Kind.LOCAL_ASSIGN, fromNode, toNode));
                            }
                        }
                    } else if (t instanceof ContextField ctxField) {
                        LocalVarNode varNode = fetchVar(s);
                        addOutEdge(new Edge(Kind.INSTANCE_STORE, varNode, ctxField));
                    }
                });
            } else if (s instanceof ContextField ctxField) {
                ts.forEach(t -> {
                    assert localVarBase(t);
                    LocalVarNode varNode = fetchVar(t);
                    addOutEdge(new Edge(Kind.INSTANCE_LOAD, ctxField, varNode));
                });
            }
        });

        pta.getCallGraph().forEach(e -> {
            Stmt callsite = e.srcStmt();
            SootMethod caller = e.src();
            if (caller != null) {
                SootMethod callee = e.tgt();
                if (!callee.isStatic()) {
                    MethodNodeFactory calleeNF = pta.getPag().getMethodPAG(callee).nodeFactory();
                    LocalVarNode thisVar = (LocalVarNode) calleeNF.caseThis();
                    InvokeExpr ie = callsite.getInvokeExpr();
                    Value base = null;
                    if (ie instanceof InstanceInvokeExpr iie) {
                        base = iie.getBase();
                    }
                    if (base != null) {
                        LocalVarNode fromNode = (LocalVarNode) pta.getPag().findValNode(base);
                        addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, thisVar));
                    }
                }
            } else if (Global.isDebug()) {
                System.out.println("Null caller of: " + callsite);
            }
        });
    }
}
