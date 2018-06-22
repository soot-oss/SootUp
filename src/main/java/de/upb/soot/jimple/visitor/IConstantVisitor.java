package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.MethodHandle;
import de.upb.soot.jimple.common.constant.ClassConstant;
import de.upb.soot.jimple.common.constant.DoubleConstant;
import de.upb.soot.jimple.common.constant.FloatConstant;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.constant.NullConstant;
import de.upb.soot.jimple.common.constant.StringConstant;

public interface IConstantVisitor extends IVisitor {
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
