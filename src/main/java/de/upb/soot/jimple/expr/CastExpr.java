package de.upb.soot.jimple.expr;

import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;

public interface CastExpr extends Expr
{
    public Value getOp();
    public void setOp(Value op);
    public ValueBox getOpBox();
    public Type getCastType();
    public void setCastType(Type castType);
    public Type getType();
    public void accept(IVisitor sw);
}
