package sootup.spark.node;

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.ValueVisitor;

public abstract class AbstractNodeValueVisitor implements ValueVisitor {
    @Override
    public void caseBooleanConstant(@NonNull BooleanConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseDoubleConstant(@NonNull DoubleConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseFloatConstant(@NonNull FloatConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseIntConstant(@NonNull IntConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseLongConstant(@NonNull LongConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseNullConstant(@NonNull NullConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseStringConstant(@NonNull StringConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseEnumConstant(@NonNull EnumConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseClassConstant(@NonNull ClassConstant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseMethodHandle(@NonNull MethodHandle handle) {
        defaultCaseValue(handle);
    }

    @Override
    public void caseMethodType(@NonNull MethodType methodType) {
        defaultCaseValue(methodType);
    }

    @Override
    public void defaultCaseConstant(@NonNull Constant constant) {
        defaultCaseValue(constant);
    }

    @Override
    public void caseAddExpr(JAddExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseAndExpr(JAndExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseCmpExpr(JCmpExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseCmpgExpr(JCmpgExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseCmplExpr(JCmplExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseDivExpr(JDivExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseEqExpr(JEqExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseNeExpr(JNeExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseGeExpr(JGeExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseGtExpr(JGtExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseLeExpr(JLeExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseLtExpr(JLtExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseMulExpr(JMulExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseOrExpr(JOrExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseRemExpr(JRemExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseShlExpr(JShlExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseShrExpr(JShrExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseUshrExpr(JUshrExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseSubExpr(JSubExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseXorExpr(JXorExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseInstanceOfExpr(JInstanceOfExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseLengthExpr(JLengthExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void caseNegExpr(JNegExpr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void casePhiExpr(JPhiExpr v) {
        defaultCaseValue(v);
    }

    @Override
    public void defaultCaseExpr(Expr expr) {
        defaultCaseValue(expr);
    }

    @Override
    public void defaultCaseRef(Ref ref) {
        defaultCaseValue(ref);
    }

    @Override
    public void defaultCaseValue(@NonNull Value v) {

    }
}
