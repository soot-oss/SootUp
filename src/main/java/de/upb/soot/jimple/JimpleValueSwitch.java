
package de.upb.soot.jimple;

public interface JimpleValueSwitch extends ConstantSwitch,
    ExprSwitch, RefSwitch
{
    public abstract void caseLocal(Local l);

}

