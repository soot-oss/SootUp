package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.expr.ArrayRef;
import de.upb.soot.jimple.ref.CaughtExceptionRef;
import de.upb.soot.jimple.ref.InstanceFieldRef;
import de.upb.soot.jimple.ref.ParameterRef;
import de.upb.soot.jimple.ref.StaticFieldRef;
import de.upb.soot.jimple.ref.ThisRef;

public abstract class AbstractRefVisitor implements IRefVisitor
{
    @Override
    public void caseArrayRef(ArrayRef v)
    {
        defaultCase(v);
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef v)
    {
        defaultCase(v);
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef v)
    {
        defaultCase(v);
    }

    @Override
    public void caseParameterRef(ParameterRef v)
    {
        defaultCase(v);
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef v)
    {
        defaultCase(v);
    }

    @Override
    public void caseThisRef(ThisRef v)
    {
        defaultCase(v);
    }

    @Override
    public void defaultCase(Object obj)
    {
    }
}

