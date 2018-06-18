package de.upb.soot.jimple;

import de.upb.soot.jimple.type.Type;

public interface CaughtExceptionRef extends IdentityRef
{
    public Type getType();
    public void apply(Switch sw);
}
