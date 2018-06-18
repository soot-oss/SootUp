package de.upb.soot.jimple;

import de.upb.soot.UnitPrinter;
import de.upb.soot.core.Unit;
import de.upb.soot.core.ValueBox;

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

