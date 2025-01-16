package sootup.java.core.exceptions;

import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.visitor.AbstractRefVisitor;

import javax.annotation.Nonnull;

public class ExceptionInferRefVisitor extends AbstractRefVisitor {

    protected ExceptionInferResult result = ExceptionInferResult.createDefaultResult();;

    public ExceptionInferRefVisitor() {}

    @Override
    public void caseStaticFieldRef(@Nonnull JStaticFieldRef ref) {
        defaultCaseRef(ref);
    }

    @Override
    public void caseInstanceFieldRef(@Nonnull JInstanceFieldRef ref) {
        defaultCaseRef(ref);
    }

    @Override
    public void caseArrayRef(@Nonnull JArrayRef ref) {
        defaultCaseRef(ref);
    }

    @Override
    public void caseParameterRef(@Nonnull JParameterRef ref) {
        defaultCaseRef(ref);
    }

    @Override
    public void caseCaughtExceptionRef(@Nonnull JCaughtExceptionRef ref) {
        defaultCaseRef(ref);
    }

    @Override
    public void caseThisRef(@Nonnull JThisRef ref) {
        defaultCaseRef(ref);
    }


    @Override
    public void defaultCaseRef(@Nonnull Ref ref) { }

    public ExceptionInferResult getResult() {
        return result;
    }

}
