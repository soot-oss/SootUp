package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.MethodHandle;
import de.upb.soot.jimple.common.constant.ClassConstant;
import de.upb.soot.jimple.common.constant.DoubleConstant;
import de.upb.soot.jimple.common.constant.FloatConstant;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.constant.NullConstant;
import de.upb.soot.jimple.common.constant.StringConstant;

public abstract class AbstractConstantVisitor implements IConstantVisitor
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
