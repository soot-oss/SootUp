package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;

/**
 * Represents a simple variable node (Green) in the pointer assignment graph that is not associated with any particular
 * method invocation.
 */
public class GlobalVariableNode extends VariableNode{
    public GlobalVariableNode(Object variable, Type type){
        super(variable, type);
    }

    public String toString() {
        return "GlobalVarNode " + variable;
    }

    public ClassType getDeclaringClassType() {
        if (variable instanceof SootField) {
            return ((SootField) variable).getDeclaringClassType();
        }

        return null;
    }
}
