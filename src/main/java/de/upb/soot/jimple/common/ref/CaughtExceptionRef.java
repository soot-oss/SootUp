package de.upb.soot.jimple.common.ref;

import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.visitor.IVisitor;

public interface CaughtExceptionRef extends IdentityRef
{
    public Type getType();
    public void accept(IVisitor sw);
}
