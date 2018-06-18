package de.upb.soot.jimple.stmt;

import de.upb.soot.UnitPrinter;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.expr.ArrayRef;
import de.upb.soot.jimple.expr.InvokeExpr;
import de.upb.soot.jimple.ref.FieldRef;

public interface Stmt extends Unit
{
    public void toString(UnitPrinter up);

    public boolean containsInvokeExpr();
    public InvokeExpr getInvokeExpr();
    public ValueBox getInvokeExprBox();

    public boolean containsArrayRef();
    public ArrayRef getArrayRef();
    public ValueBox getArrayRefBox();

    public boolean containsFieldRef();
    public FieldRef getFieldRef();
    public ValueBox getFieldRefBox();
}

