package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.common.ref.AbstractInstanceFieldRef;
import de.upb.soot.jimple.common.ref.ArrayRef;
import de.upb.soot.jimple.common.ref.CaughtExceptionRef;
import de.upb.soot.jimple.common.ref.ParameterRef;
import de.upb.soot.jimple.common.ref.StaticFieldRef;
import de.upb.soot.jimple.common.ref.ThisRef;

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
  public void caseInstanceFieldRef(AbstractInstanceFieldRef v)
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

