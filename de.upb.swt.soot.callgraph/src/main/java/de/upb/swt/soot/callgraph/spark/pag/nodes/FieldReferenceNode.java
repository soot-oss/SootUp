package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;

import java.util.HashSet;
import java.util.Set;

public class FieldReferenceNode extends Node {

    private VariableNode base;
    private Field field;


    public FieldReferenceNode(VariableNode base, Field field, Type type) {
       this.base = base;
       this.field = field;
       this.type = type;
       this.base.addField(this);
    }


}
