package de.upb.soot.jimple;

public abstract class AbstractExprSwitch implements ExprSwitch
{
    Object result;

    @Override
    public void caseAddExpr(AddExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseAndExpr(AndExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseCmpExpr(CmpExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseCmpgExpr(CmpgExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseCmplExpr(CmplExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseDivExpr(DivExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseEqExpr(EqExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNeExpr(NeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseGeExpr(GeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseGtExpr(GtExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseLeExpr(LeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseLtExpr(LtExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseMulExpr(MulExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseOrExpr(OrExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseRemExpr(RemExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseShlExpr(ShlExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseShrExpr(ShrExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseUshrExpr(UshrExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseSubExpr(SubExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseXorExpr(XorExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseSpecialInvokeExpr(SpecialInvokeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseStaticInvokeExpr(StaticInvokeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseVirtualInvokeExpr(VirtualInvokeExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
    	defaultCase(v);
    }

    @Override
    public void caseCastExpr(CastExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseInstanceOfExpr(InstanceOfExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNewArrayExpr(NewArrayExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNewMultiArrayExpr(NewMultiArrayExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNewExpr(NewExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseLengthExpr(LengthExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void caseNegExpr(NegExpr v)
    {
        defaultCase(v);
    }

    @Override
    public void defaultCase(Object obj)
    {
    }

    public void setResult(Object result)
    {
        this.result = result;
    }

    public Object getResult()
    {
        return result;
    }
}
