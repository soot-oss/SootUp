package qilin.pta.toolkits.debloaterx;

import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.util.PTAUtils;
import qilin.util.queue.UniqueQueue;
import soot.RefLikeType;
import soot.RefType;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.spark.pag.SparkField;
import soot.util.queue.QueueReader;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XPAG {
    /* record nodes and edges in the graph */
    protected final Map<Node, Set<Edge>> outEdges = new ConcurrentHashMap<>();
    protected final PTA pta;
    protected final PAG pag;
    private final LocalVarNode dummyThis;
    protected final XUtility utility;

    public XPAG(PTA pta, XUtility utility) {
        this.pta = pta;
        this.pag = pta.getPag();
        this.utility = utility;
        this.dummyThis = new LocalVarNode("DUMMYTHIS", RefType.v("java.lang.Object"), null);
        buildGraph(pta.getNakedReachableMethods());
    }

    /*
     * Constructing the XPAG for a program (Figure 5 in the paper)
     * */
    protected void buildGraph(Collection<SootMethod> reachables) {
        // add edges in each method
        reachables.parallelStream().forEach(this::buildInternal);
    }

    protected void buildInternal(SootMethod method) {
        buildInternalWithInline(method);
    }

    protected void buildInternalWithInline(SootMethod method) {
        MethodPAG srcmpag = pag.getMethodPAG(method);
        MethodNodeFactory srcnf = srcmpag.nodeFactory();
        VarNode thisNode = srcnf.caseThis();
        // add special ``this'' edge
        addThisEdge((LocalVarNode) thisNode);
        // add normal edges: NEW, ASSIGN, LOAD, STORE
        QueueReader<Node> reader = srcmpag.getInternalReader().clone();
        while (reader.hasNext()) {
            Node from = reader.next(), to = reader.next();
            if (from instanceof LocalVarNode) {
                if (to instanceof LocalVarNode) {
                    this.addAssignEdge((LocalVarNode) from, (LocalVarNode) to);
                } else if (to instanceof FieldRefNode fr) {
                    this.addStoreEdge((LocalVarNode) from, (LocalVarNode) fr.getBase(), fr.getField());
                }  // local-global

            } else if (from instanceof AllocNode) {
                if (to instanceof LocalVarNode) {
                    this.addNewEdge((AllocNode) from, (LocalVarNode) to);
                } // GlobalVarNode
            } else if (from instanceof FieldRefNode fr) {
                this.addLoadEdge((LocalVarNode) fr.getBase(), (LocalVarNode) to, fr.getField());
            }  // global-local
        }
        // handle call statements.
        for (final Unit u : srcmpag.getInvokeStmts()) {
            final Stmt s = (Stmt) u;
            InvokeExpr ie = s.getInvokeExpr();
            int numArgs = ie.getArgCount();
            Value[] args = new Value[numArgs];
            for (int i = 0; i < numArgs; i++) {
                Value arg = ie.getArg(i);
                if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant)
                    continue;
                args[i] = arg;
            }
            LocalVarNode retDest = null;
            if (s instanceof AssignStmt) {
                Value dest = ((AssignStmt) s).getLeftOp();
                if (dest.getType() instanceof RefLikeType) {
                    retDest = pag.findLocalVarNode(dest);
                }
            }
            if (ie instanceof InstanceInvokeExpr iie) {
                LocalVarNode receiver = pag.findLocalVarNode(iie.getBase());
                if (iie instanceof SpecialInvokeExpr sie) {
                    inline(s, sie.getMethod());
                } else {
                    /* instance call with non-this base variable are modeled as in Eagle/Turner. */
                    modelVirtualCall(numArgs, args, receiver, retDest);
                }
            } else {
                if (ie instanceof StaticInvokeExpr sie) {
                    SootMethod target = sie.getMethod();
                    inline(s, target);
                }
            }
        }
        // handle parameters.
        for (int i = 0; i < method.getParameterCount(); ++i) {
            if (method.getParameterType(i) instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getParameterType(i))) {
                LocalVarNode param = (LocalVarNode) srcnf.caseParm(i);
                addParamEdge(param);
            }
        }
        // treat this as a special parameter.
        if (!method.isStatic()) {
            addParamEdge((LocalVarNode) srcnf.caseThis());
        }
        // handle returns
        if (method.getReturnType() instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getReturnType())) {
            addReturnEdge((LocalVarNode) srcnf.caseRet());
        }
    }

    private void modelVirtualCall(int numArgs, Value[] args, LocalVarNode receiver, LocalVarNode retDest) {
        for (int i = 0; i < numArgs; i++) {
            if (args[i] == null) {
                continue;
            }
            ValNode argNode = pag.findValNode(args[i]);
            if (argNode instanceof LocalVarNode) {
                addCStoreEdge((LocalVarNode) argNode, receiver);
            }
        }
        if (retDest != null) {
            addCLoadEdge(receiver, retDest);
        }
        addCStoreEdge(receiver, receiver);
    }

    private void inline(Stmt invokeStmt, SootMethod method) {
        InvokeExpr ie = invokeStmt.getInvokeExpr();
        int numArgs = ie.getArgCount();
        Value[] args = new Value[numArgs];
        for (int i = 0; i < numArgs; i++) {
            Value arg = ie.getArg(i);
            if (!(arg.getType() instanceof RefLikeType) || arg instanceof NullConstant)
                continue;
            args[i] = arg;
        }
        LocalVarNode retDest = null;
        if (invokeStmt instanceof AssignStmt) {
            Value dest = ((AssignStmt) invokeStmt).getLeftOp();
            if (dest.getType() instanceof RefLikeType) {
                retDest = pag.findLocalVarNode(dest);
            }
        }
        LocalVarNode receiver = null;
        if (ie instanceof InstanceInvokeExpr iie) {
            receiver = pag.findLocalVarNode(iie.getBase());
        }
        MethodPAG mpag = pag.getMethodPAG(method);
        MethodNodeFactory nodeFactory = mpag.nodeFactory();
        if (numArgs != method.getParameterCount()) {
            return;
        }
        // handle parameters
        for (int i = 0; i < method.getParameterCount(); ++i) {
            if (args[i] != null && method.getParameterType(i) instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getParameterType(i))) {
                LocalVarNode param = (LocalVarNode) nodeFactory.caseParm(i);
                ValNode argVal = pag.findValNode(args[i]);
                if (argVal instanceof LocalVarNode argNode) {
                    addAssignEdge(argNode, param);
                }
            }
        }
        // handle return node
        if (retDest != null && method.getReturnType() instanceof RefLikeType && !PTAUtils.isPrimitiveArrayType(method.getReturnType())) {
            addAssignEdge((LocalVarNode) nodeFactory.caseRet(), retDest);
        }
        // handle this node
        if (receiver != null) {
            addAssignEdge(receiver, (LocalVarNode) nodeFactory.caseThis());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void addNormalEdge(Edge edge) {
        outEdges.computeIfAbsent(edge.from, k -> ConcurrentHashMap.newKeySet()).add(edge);
    }

    /* methods for adding normal edges: NEW, ASSIGN, STORE, LOAD. */
    protected void addNewEdge(AllocNode from, LocalVarNode to) {
        // skip merged heaps.
        if (from.getMethod() == null && !(from instanceof ConstantNode)) {
            return;
        }
        Edge newEdge = new Edge(from, to, null, EdgeKind.NEW);
        addNormalEdge(newEdge);
        Edge iNewEdge = new Edge(to, from, null, EdgeKind.INEW);
        addNormalEdge(iNewEdge);
    }

    protected void addAssignEdge(LocalVarNode from, LocalVarNode to) {
        Edge assignEdge = new Edge(from, to, null, EdgeKind.ASSIGN);
        addNormalEdge(assignEdge);
        Edge iAssignEdge = new Edge(to, from, null, EdgeKind.IASSIGN);
        addNormalEdge(iAssignEdge);
    }

    protected void addStoreEdge(LocalVarNode from, LocalVarNode base, SparkField field) {
        Edge storeEdge = new Edge(from, base, field, EdgeKind.STORE);
        addNormalEdge(storeEdge);
        Edge iStoreEdge = new Edge(base, from, field, EdgeKind.ISTORE);
        addNormalEdge(iStoreEdge);
    }

    protected void addLoadEdge(LocalVarNode base, LocalVarNode to, SparkField field) {
        Edge loadEdge = new Edge(base, to, field, EdgeKind.LOAD);
        addNormalEdge(loadEdge);
        Edge iLoadEdge = new Edge(to, base, field, EdgeKind.ILOAD);
        addNormalEdge(iLoadEdge);
    }

    /* methods for adding inter-procedural edges at non-this invocation site */
    protected void addCStoreEdge(LocalVarNode from, LocalVarNode base) {
        Edge cstoreEdge = new Edge(from, base, null, EdgeKind.CSTORE);
        addNormalEdge(cstoreEdge);
        Edge iCstoreEdge = new Edge(base, from, null, EdgeKind.ICSTORE);
        addNormalEdge(iCstoreEdge);
    }

    protected void addCLoadEdge(LocalVarNode base, LocalVarNode to) {
        Edge cLoadEdge = new Edge(base, to, null, EdgeKind.CLOAD);
        addNormalEdge(cLoadEdge);
        Edge iCloadEdge = new Edge(to, base, null, EdgeKind.ICLOAD);
        addNormalEdge(iCloadEdge);
    }

    /* this is a special edge */
    protected void addThisEdge(LocalVarNode thisNode) {
        Edge thisEdge = new Edge(thisNode, dummyThis, null, EdgeKind.THIS);
        addNormalEdge(thisEdge);
        Edge ithisEdge = new Edge(dummyThis, thisNode, null, EdgeKind.ITHIS);
        addNormalEdge(ithisEdge);
    }

    /* methods for adding self-loop edges */
    protected void addParamEdge(LocalVarNode param) {
        Edge paramEdge = new Edge(param, param, null, EdgeKind.PARAM);
        addNormalEdge(paramEdge);
    }

    protected void addReturnEdge(LocalVarNode ret) {
        Edge retEdge = new Edge(ret, ret, null, EdgeKind.RETURN);
        addNormalEdge(retEdge);
    }

    public Set<Edge> getOutEdges(Node node) {
        return this.outEdges.getOrDefault(node, Collections.emptySet());
    }

    public LocalVarNode getDummyThis() {
        return dummyThis;
    }

}
