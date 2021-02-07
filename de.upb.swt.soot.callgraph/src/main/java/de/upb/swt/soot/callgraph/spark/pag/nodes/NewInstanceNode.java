package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.types.Type;

/**
 * Node that represents a call to newInstance()
 */
public class NewInstanceNode extends Node{
    private final Value value;

    public NewInstanceNode(Type type, Value value) {
        super(type);
        this.value = value;
    }

    public Value getValue() {
        return value;
    }
}
