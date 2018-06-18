package de.upb.soot.jimple;

import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;

public interface UnopExpr extends Expr
{
    public Value getOp();
    public void setOp(Value op);
    public ValueBox getOpBox();
}
