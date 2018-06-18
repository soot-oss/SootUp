package de.upb.soot.jimple;

import de.upb.soot.core.Local;
import de.upb.soot.core.Value;
import de.upb.soot.core.ValueBox;
import de.upb.soot.jimple.type.Type;

public interface ArrayRef extends ConcreteRef
{
    public Value getBase();
    public void setBase(Local base);
    public ValueBox getBaseBox();
    public Value getIndex();
    public void setIndex(Value index);
    public ValueBox getIndexBox();
    public Type getType();
    public void apply(Switch sw);
}



