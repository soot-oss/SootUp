package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph that is specific to a particular method
 * invocation.
 *
 */
public class LocalVariableNode extends VariableNode{
    private SootMethod method;

    public LocalVariableNode(Object variable, Type type, SootMethod method) {
        super(variable, type);
        this.method = method;
    }

    public SootMethod getMethod(){
        return method;
    }

    @Override
    public String toString() {
        return "LocalVarNode " /*+ getNumber() + " "*/ + variable + " " + method;
    }


}
