package de.upb.swt.soot.callgraph.spark.builder;

import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.visitor.ConstantVisitor;

public abstract class AbstractConstantVisitor<T> implements ConstantVisitor {

    T result;

    @Override
    public void caseBooleanConstant(BooleanConstant v) { defaultCase(v); }

    @Override
    public void caseDoubleConstant(DoubleConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseFloatConstant(FloatConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseIntConstant(IntConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseLongConstant(LongConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseNullConstant(NullConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseStringConstant(StringConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseClassConstant(ClassConstant v) {
        defaultCase(v);
    }

    @Override
    public void caseMethodHandle(MethodHandle v) {
        defaultCase(v);
    }

    @Override
    public void defaultCase(Object v) {
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
