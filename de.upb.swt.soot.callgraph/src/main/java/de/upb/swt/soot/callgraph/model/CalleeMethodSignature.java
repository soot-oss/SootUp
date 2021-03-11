package de.upb.swt.soot.callgraph.model;


import de.upb.swt.soot.core.signatures.MethodSignature;

/**
 * Method Signature with its calling CallGraphEdgeType
 */
public class CalleeMethodSignature extends MethodSignature {

    private CallGraphEdgeType edgeType;

    public CalleeMethodSignature(MethodSignature methodSignature, CallGraphEdgeType edgeType){
        super(methodSignature.getDeclClassType(), methodSignature.getSubSignature());
        this.edgeType = edgeType;
    }

    public CallGraphEdgeType getEdgeType() {
        return edgeType;
    }
}
