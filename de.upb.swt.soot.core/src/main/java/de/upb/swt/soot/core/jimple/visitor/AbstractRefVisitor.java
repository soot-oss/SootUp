package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.common.ref.*;

public abstract class AbstractRefVisitor implements RefVisitor{

    @Override
    public void caseStaticFieldRef(JStaticFieldRef v){defaultCase(v);}

    @Override
    public void caseInstanceFieldRef(JInstanceFieldRef v){defaultCase(v);}

    @Override
    public void caseArrayRef(JArrayRef v){defaultCase(v);}

    @Override
    public void caseParameterRef(JParameterRef v){defaultCase(v);}

    @Override
    public void caseCaughtExceptionRef(JCaughtExceptionRef v){defaultCase(v);}

    @Override
    public void caseThisRef(JThisRef v){defaultCase(v);}

    @Override
    public void defaultCase(Object obj){}
}
