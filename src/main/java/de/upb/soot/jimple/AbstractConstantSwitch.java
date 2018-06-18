package de.upb.soot.jimple;

public abstract class AbstractConstantSwitch implements ConstantSwitch
{
    Object result;

    @Override
    public void caseDoubleConstant(DoubleConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseFloatConstant(FloatConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseIntConstant(IntConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseLongConstant(LongConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNullConstant(NullConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseStringConstant(StringConstant v)
    {
        defaultCase(v);
    }

    @Override
    public void caseClassConstant(ClassConstant v)
    {
        defaultCase(v);
    }
    
    @Override
    public void caseMethodHandle(MethodHandle v)
    {
        defaultCase(v);
    }

    @Override
    public void defaultCase(Object v)
    {
    }

    public Object getResult()
    {
        return result;
    }

    public void setResult(Object result)
    {
        this.result = result;
    }
}
