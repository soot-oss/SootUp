package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.types.Type;

public class ConcreteFieldNode extends Node{
    private AllocationNode base;
    private Field field;

    public ConcreteFieldNode(AllocationNode base, Field field, Type type) {
        this.base = base;
        this.field = field;
        this.type = type;
        base.addField(this);
    }

    public AllocationNode getBase() {
        return base;
    }

    public Field getField() {
        return field;
    }
}
