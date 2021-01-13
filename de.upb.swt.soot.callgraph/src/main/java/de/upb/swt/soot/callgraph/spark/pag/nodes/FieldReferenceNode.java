package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.types.Type;

import java.util.HashSet;
import java.util.Set;

public class FieldReferenceNode extends VariableNode {

    private Set<VariableNode> fields;

    public FieldReferenceNode(Object variable, Type type) {
        super(variable, type);
    }

    public void addField(VariableNode field){
        if(fields==null){
            fields = new HashSet<>();
        }
        fields.add(field);
    }
}
