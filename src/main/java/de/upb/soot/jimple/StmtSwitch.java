package de.upb.soot.jimple;


public interface StmtSwitch extends Switch
{
    public abstract void caseBreakpointStmt(BreakpointStmt stmt);
    public abstract void caseInvokeStmt(InvokeStmt stmt);
    public abstract void caseAssignStmt(AssignStmt stmt);
    public abstract void caseIdentityStmt(IdentityStmt stmt);
    public abstract void caseEnterMonitorStmt(EnterMonitorStmt stmt);
    public abstract void caseExitMonitorStmt(ExitMonitorStmt stmt);
    public abstract void caseGotoStmt(GotoStmt stmt);
    public abstract void caseIfStmt(IfStmt stmt);
    public abstract void caseLookupSwitchStmt(LookupSwitchStmt stmt);
    public abstract void caseNopStmt(NopStmt stmt);
    public abstract void caseRetStmt(RetStmt stmt);
    public abstract void caseReturnStmt(ReturnStmt stmt);
    public abstract void caseReturnVoidStmt(ReturnVoidStmt stmt);
    public abstract void caseTableSwitchStmt(TableSwitchStmt stmt);
    public abstract void caseThrowStmt(ThrowStmt stmt);
    public abstract void defaultCase(Object obj);
}
