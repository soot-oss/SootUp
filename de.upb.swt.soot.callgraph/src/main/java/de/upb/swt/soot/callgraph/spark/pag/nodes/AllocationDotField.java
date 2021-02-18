package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.Field;

public class AllocationDotField extends Node {
    private AllocationNode base;
    private Field field;

    public AllocationDotField(AllocationNode base, Field field) {
        this.base = base;
        this.field = field;
        base.addField(this, field);
    }

    public AllocationNode getBase(){
        return base;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return "AllocationDotField{" +
                "base=" + base +
                ", field=" + field +
                '}';
    }
}
