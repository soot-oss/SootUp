package qilin.pta.toolkits.zipper.flowgraph;

import java.util.*;
import qilin.core.PTA;
import qilin.core.builder.MethodNodeFactory;
import qilin.core.pag.*;
import qilin.pta.toolkits.zipper.Global;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;

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
    if (valNode instanceof ContextVarNode) {
      ContextVarNode cvn = (ContextVarNode) valNode;
      return cvn.base() instanceof LocalVarNode;
    } else {
      return valNode instanceof LocalVarNode;
    }
  }

  private LocalVarNode fetchLocalVar(ValNode valNode) {
    if (valNode instanceof ContextVarNode) {
      ContextVarNode cvn = (ContextVarNode) valNode;
      if (cvn.base() instanceof LocalVarNode) {
        return (LocalVarNode) cvn.base();
      }
    } else if (valNode instanceof LocalVarNode) {
      return (LocalVarNode) valNode;
    }
    return null;
  }

  private LocalVarNode fetchVar(ValNode valNode) {
    if (valNode instanceof ContextVarNode) {
      ContextVarNode cvn = (ContextVarNode) valNode;
      VarNode base = cvn.base();
      if (base instanceof LocalVarNode) {
        return (LocalVarNode) base;
      }
    } else if (valNode instanceof LocalVarNode) {
      return (LocalVarNode) valNode;
    }
    return null;
  }

  public void addOutEdge(final Edge e) {
    outEdges.computeIfAbsent(e.getSource(), k -> new HashSet<>()).add(e);
  }

  private void init() {
    outEdges = new HashMap<>();

    pta.getPag()
        .getSimple()
        .forEach(
            (s, ts) -> {
              if (localVarBase(s)) {
                ts.forEach(
                    t -> {
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
                      } else if (t instanceof ContextField) {
                        ContextField ctxField = (ContextField) t;
                        LocalVarNode varNode = fetchVar(s);
                        addOutEdge(new Edge(Kind.INSTANCE_STORE, varNode, ctxField));
                      }
                    });
              } else if (s instanceof ContextField) {
                ContextField ctxField = (ContextField) s;
                ts.forEach(
                    t -> {
                      assert localVarBase(t);
                      LocalVarNode varNode = fetchVar(t);
                      addOutEdge(new Edge(Kind.INSTANCE_LOAD, ctxField, varNode));
                    });
              }
            });

    pta.getCallGraph()
        .forEach(
            e -> {
              InvokableStmt callsite = e.srcStmt();
              SootMethod caller = e.src();
              if (caller != null) {
                SootMethod callee = e.tgt();
                if (!callee.isStatic()) {
                  MethodNodeFactory calleeNF = pta.getPag().getMethodPAG(callee).nodeFactory();
                  LocalVarNode thisVar = (LocalVarNode) calleeNF.caseThis();
                  AbstractInvokeExpr ie = callsite.getInvokeExpr().get();
                  Value base = null;
                  if (ie instanceof AbstractInstanceInvokeExpr) {
                    AbstractInstanceInvokeExpr iie = (AbstractInstanceInvokeExpr) ie;
                    base = iie.getBase();
                  }
                  if (base != null) {
                    LocalVarNode fromNode = (LocalVarNode) pta.getPag().findValNode(base, caller);
                    addOutEdge(new Edge(Kind.INTERPROCEDURAL_ASSIGN, fromNode, thisVar));
                  }
                }
              } else if (Global.isDebug()) {
                System.out.println("Null caller of: " + callsite);
              }
            });
  }
}
