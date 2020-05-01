package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.common.ref.*;

public interface RefVisitor extends Visitor {

    void caseStaticFieldRef(JStaticFieldRef v);

    void caseInstanceFieldRef(JInstanceFieldRef v);

    void caseArrayRef(JArrayRef v);

    void caseParameterRef(JParameterRef v);

    void caseCaughtExceptionRef(JCaughtExceptionRef v);

    void caseThisRef(JThisRef v);

    void defaultCase(Object obj);
}
