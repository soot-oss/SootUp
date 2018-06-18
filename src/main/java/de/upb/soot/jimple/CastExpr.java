package de.upb.soot.jimple;

import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;
import de.upb.soot.jimple.type.Type;

public interface CastExpr extends Expr
{
    public Value getOp();
    public void setOp(Value op);
    public ValueBox getOpBox();
    public Type getCastType();
    public void setCastType(Type castType);
    public Type getType();
    public void apply(Switch sw);
}
