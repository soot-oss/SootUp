package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.Type;

import java.util.HashSet;
import java.util.Set;

public class AllocationNode extends Node {
    /*
    Each allocation node has an associated type, and all objects that it represents are
    expected to have exactly this type at run-time (not a subtype)
     */
    // TODO: [kk] old soot used Object instead of JNewExpr
    private JNewExpr newExpr;
    private SootMethod method;
    private Set<ConcreteFieldNode> fields;

    public AllocationNode(Type type, JNewExpr newExpr, SootMethod method) {
        this.type = type;
        this.newExpr = newExpr;
        this.method = method;
    }

    public JNewExpr getNewExpr() {
        return newExpr;
    }

    public SootMethod getMethod() {
        return method;
    }

    public void addField(ConcreteFieldNode concreteFieldNode){
        if(fields == null){
            fields = new HashSet<>();
        }
        fields.add(concreteFieldNode);
    }
}
