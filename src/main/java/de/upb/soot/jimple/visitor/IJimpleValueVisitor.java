
package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.Local;

public interface IJimpleValueVisitor extends IConstantVisitor,
    IExprVisitor, IRefVisitor
{
    public abstract void caseLocal(Local l);

}

