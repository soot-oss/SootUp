package de.upb.swt.soot.callgraph.model;


import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.signatures.MethodSignature;

/**
 * Method Signature with its calling CallGraphEdgeType
 */
public class CalleeMethodSignature extends MethodSignature {

    private CallGraphEdgeType edgeType;

    /**
     * The unit at which the call occurs; may be null for calls not occurring at a specific statement (eg. calls in native
     * code)
     */
    private Stmt sourceStmt;

    public CalleeMethodSignature(MethodSignature methodSignature, CallGraphEdgeType edgeType, Stmt sourceStmt){
        super(methodSignature.getDeclClassType(), methodSignature.getSubSignature());
        this.edgeType = edgeType;
        this.sourceStmt = sourceStmt;
    }

    public CallGraphEdgeType getEdgeType() {
        return edgeType;
    }

    public Stmt getSourceStmt() {
        return sourceStmt;
    }
}
