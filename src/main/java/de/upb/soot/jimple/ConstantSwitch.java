package de.upb.soot.jimple;

public interface ConstantSwitch extends Switch
{
    public abstract void caseDoubleConstant(DoubleConstant v);
    public abstract void caseFloatConstant(FloatConstant v);
    public abstract void caseIntConstant(IntConstant v);
    public abstract void caseLongConstant(LongConstant v);
    public abstract void caseNullConstant(NullConstant v);
    public abstract void caseStringConstant(StringConstant v);
    public abstract void caseClassConstant(ClassConstant v);
    public abstract void caseMethodHandle(MethodHandle handle);
    public abstract void defaultCase(Object object);
}
