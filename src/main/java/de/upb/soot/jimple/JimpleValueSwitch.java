
package de.upb.soot.jimple;
import de.upb.soot.core.Local;

public interface JimpleValueSwitch extends ConstantSwitch,
    ExprSwitch, RefSwitch
{
    public abstract void caseLocal(Local l);

}

