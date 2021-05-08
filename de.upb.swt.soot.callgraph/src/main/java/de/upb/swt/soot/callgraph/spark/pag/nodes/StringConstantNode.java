package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.callgraph.spark.builder.NodeConstants;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;

/**
 * Represents an allocation site node the represents a constant string.
 */
public class StringConstantNode extends AllocationNode {
    public StringConstantNode(String sc) {
        super(
                JavaIdentifierFactory.getInstance().getClassType(NodeConstants.CLASS),
                sc,
                null);
    }

    public String getString() {
        return (String) getNewExpr();
    }

    public String toString() {
        return "StringConstantNode " + getNewExpr();
    }

}
