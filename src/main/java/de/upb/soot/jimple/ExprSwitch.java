package de.upb.soot.jimple;

public interface ExprSwitch extends Switch
{
    public abstract void caseAddExpr(AddExpr v);
    public abstract void caseAndExpr(AndExpr v);
    public abstract void caseCmpExpr(CmpExpr v);
    public abstract void caseCmpgExpr(CmpgExpr v);
    public abstract void caseCmplExpr(CmplExpr v);
    public abstract void caseDivExpr(DivExpr v);
    public abstract void caseEqExpr(EqExpr v);
    public abstract void caseNeExpr(NeExpr v);
    public abstract void caseGeExpr(GeExpr v);
    public abstract void caseGtExpr(GtExpr v);
    public abstract void caseLeExpr(LeExpr v);
    public abstract void caseLtExpr(LtExpr v);
    public abstract void caseMulExpr(MulExpr v);
    public abstract void caseOrExpr(OrExpr v);
    public abstract void caseRemExpr(RemExpr v);
    public abstract void caseShlExpr(ShlExpr v);
    public abstract void caseShrExpr(ShrExpr v);
    public abstract void caseUshrExpr(UshrExpr v);
    public abstract void caseSubExpr(SubExpr v);
    public abstract void caseXorExpr(XorExpr v);
    public abstract void caseInterfaceInvokeExpr(InterfaceInvokeExpr v);
    public abstract void caseSpecialInvokeExpr(SpecialInvokeExpr v);
    public abstract void caseStaticInvokeExpr(StaticInvokeExpr v);
    public abstract void caseVirtualInvokeExpr(VirtualInvokeExpr v);
    public abstract void caseDynamicInvokeExpr(DynamicInvokeExpr v);
    public abstract void caseCastExpr(CastExpr v);
    public abstract void caseInstanceOfExpr(InstanceOfExpr v);
    public abstract void caseNewArrayExpr(NewArrayExpr v);
    public abstract void caseNewMultiArrayExpr(NewMultiArrayExpr v);
    public abstract void caseNewExpr(NewExpr v);
    public abstract void caseLengthExpr(LengthExpr v);
    public abstract void caseNegExpr(NegExpr v);
    public abstract void defaultCase(Object obj);
}
