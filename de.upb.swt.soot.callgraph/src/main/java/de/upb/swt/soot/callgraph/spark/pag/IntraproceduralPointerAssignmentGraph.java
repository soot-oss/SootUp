package de.upb.swt.soot.callgraph.spark.pag;

import com.sun.javafx.geom.Edge;
import de.upb.swt.soot.callgraph.spark.builder.MethodNodeFactory;
import de.upb.swt.soot.callgraph.spark.pag.nodes.Node;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JStaticInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JBreakpointStmt;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.views.View;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import sun.jvm.hotspot.debugger.cdbg.RefType;
import sun.security.provider.certpath.Vertex;

import javax.annotation.Nonnull;

public class IntraproceduralPointerAssignmentGraph {

    private PointerAssignmentGraph pag;
    private DefaultDirectedGraph<SparkVertex, SparkEdge> graph;
    private MethodNodeFactory nodeFactory;

    private SootMethod method;

    public IntraproceduralPointerAssignmentGraph(PointerAssignmentGraph pag, SootMethod method){
        this.pag = pag;
        this.method = method;
        this.nodeFactory = new MethodNodeFactory(this);
    }

    public void build(){
        if(method.isConcrete()){
            Body body = method.getBody();
            for(Stmt stmt: body.getStmts()){
                nodeFactory.processStmt(stmt);
            }
        } else {
            // TODO: build for native
        }
    }

    public DefaultDirectedGraph<SparkVertex, SparkEdge> getGraph(){
        return graph;
    }



    public SootMethod getMethod(){
        return method;
    }

    public PointerAssignmentGraph getPointerAssignmentGraph(){
        return pag;
    }

}
