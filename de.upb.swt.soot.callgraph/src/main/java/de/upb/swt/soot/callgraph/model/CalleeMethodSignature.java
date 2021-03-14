package de.upb.swt.soot.callgraph.model;


import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.signatures.MethodSignature;

/**
 * Method Signature with its calling CallGraphEdgeType
 */
public class CalleeMethodSignature {

    private CallGraphEdgeType edgeType;
    private MethodSignature methodSignature;

    /**
     * The unit at which the call occurs; may be null for calls not occurring at a specific statement (eg. calls in native
     * code)
     */
    private Stmt sourceStmt;

    public CalleeMethodSignature(MethodSignature methodSignature, CallGraphEdgeType edgeType, Stmt sourceStmt){
        this.methodSignature = methodSignature;
        this.edgeType = edgeType;
        this.sourceStmt = sourceStmt;
    }

    public MethodSignature getMethodSignature() {
        return methodSignature;
    }

    public CallGraphEdgeType getEdgeType() {
        return edgeType;
    }

    public Stmt getSourceStmt() {
        return sourceStmt;
    }
}
