package de.upb.soot.jimple.expr;

import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;

public interface UnopExpr extends Expr
{
    public Value getOp();
    public void setOp(Value op);
    public ValueBox getOpBox();
}
