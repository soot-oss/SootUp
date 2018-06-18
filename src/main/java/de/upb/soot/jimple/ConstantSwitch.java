package de.upb.soot.jimple;

import de.upb.soot.jimple.constant.ClassConstant;
import de.upb.soot.jimple.constant.DoubleConstant;
import de.upb.soot.jimple.constant.FloatConstant;
import de.upb.soot.jimple.constant.IntConstant;
import de.upb.soot.jimple.constant.LongConstant;
import de.upb.soot.jimple.constant.NullConstant;
import de.upb.soot.jimple.constant.StringConstant;

public interface ConstantSwitch extends IVisitor
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
