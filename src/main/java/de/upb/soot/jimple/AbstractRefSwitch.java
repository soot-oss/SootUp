package de.upb.soot.jimple;

public abstract class AbstractRefSwitch implements RefSwitch
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

