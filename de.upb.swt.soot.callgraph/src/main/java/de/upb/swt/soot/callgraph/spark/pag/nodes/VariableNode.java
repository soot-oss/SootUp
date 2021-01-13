package de.upb.swt.soot.callgraph.spark.pag.nodes;

import com.sun.org.apache.xpath.internal.operations.Variable;
import de.upb.swt.soot.core.types.Type;

import java.util.HashSet;
import java.util.Set;

public class VariableNode extends Node {

    // TODO: [kk] need a more precise type, or generics
    private Object variable;


    public VariableNode(Object variable, Type type) {
        this.variable = variable;
        this.type = type;
    }

}
