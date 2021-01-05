package de.upb.swt.soot.callgraph.spark.sets;

public abstract class P2SetVisitor {
    public abstract void visit(Node n);

    public boolean getReturnValue() {
        return returnValue;
    }

    protected boolean returnValue = false;
}
