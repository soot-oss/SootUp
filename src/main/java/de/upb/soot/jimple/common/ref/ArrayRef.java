package de.upb.soot.jimple.expr;

import de.upb.soot.jimple.Local;
import de.upb.soot.jimple.Value;
import de.upb.soot.jimple.ValueBox;
import de.upb.soot.jimple.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;

public interface ArrayRef extends ConcreteRef
{
    public Value getBase();
    public void setBase(Local base);
    public ValueBox getBaseBox();
    public Value getIndex();
    public void setIndex(Value index);
    public ValueBox getIndexBox();
    public Type getType();
    public void accept(IVisitor sw);
}



