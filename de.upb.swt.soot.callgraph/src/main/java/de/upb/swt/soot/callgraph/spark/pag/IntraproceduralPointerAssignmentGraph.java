package de.upb.swt.soot.callgraph.spark.pag;

import com.sun.javafx.geom.Edge;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import sun.jvm.hotspot.debugger.cdbg.RefType;
import sun.security.provider.certpath.Vertex;

import javax.annotation.Nonnull;

public class IntraproceduralPointerAssignmentGraph {

    private DefaultDirectedGraph<SparkVertex, SparkEdge> graph;

    private SootMethod method;

    public IntraproceduralPointerAssignmentGraph(SootMethod method){
        // TODO: need chaching?
        this.method = method;
    }

    public void build(){
        if(method.isConcrete()){
            Body body = method.getBody();
            for(Stmt stmt: body.getStmts()){
                processStmt(stmt);
            }
        } else {
            // TODO: build for native
        }
    }

    private void processStmt(Stmt stmt){
        // TODO: types-for-invoke
        if(stmt.containsInvokeExpr()){
            AbstractInvokeExpr invokeExpr = stmt.getInvokeExpr();
            if(invokeExpr instanceof JVirtualInvokeExpr){
                if(!isReflectionNewInstance(invokeExpr)){
                    return;
                }
            } else if (!(invokeExpr instanceof JStaticInvokeExpr)){
                return;
            }
        }
    }

    public DefaultDirectedGraph<SparkVertex, SparkEdge> getGraph(){
        return graph;
    }

    /**
     * Checks whether the given invocation is for Class.newInstance()
     *
     * @param invokeExpr
     * @return
     */
    private boolean isReflectionNewInstance(AbstractInvokeExpr invokeExpr){
        // TODO: put this in a utility class?
        if(invokeExpr instanceof JVirtualInvokeExpr){
            JVirtualInvokeExpr virtualInvokeExpr = (JVirtualInvokeExpr) invokeExpr;
            if (virtualInvokeExpr.getBase().getType() instanceof RefType) {
                RefType rt = (RefType) virtualInvokeExpr.getBase().getType();
                if (rt.getClass().getName().equals("java.lang.Class")) {
                    MethodSignature signature = virtualInvokeExpr.getMethodSignature();
                    if (signature.getName().equals("newInstance") && signature.getParameterTypes().size() == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
