package de.upb.soot.jimple;

import de.upb.soot.jimple.stmt.AssignStmt;
import de.upb.soot.jimple.stmt.BreakpointStmt;
import de.upb.soot.jimple.stmt.EnterMonitorStmt;
import de.upb.soot.jimple.stmt.ExitMonitorStmt;
import de.upb.soot.jimple.stmt.GotoStmt;
import de.upb.soot.jimple.stmt.IdentityStmt;
import de.upb.soot.jimple.stmt.IfStmt;
import de.upb.soot.jimple.stmt.InvokeStmt;
import de.upb.soot.jimple.stmt.LookupSwitchStmt;
import de.upb.soot.jimple.stmt.NopStmt;
import de.upb.soot.jimple.stmt.RetStmt;
import de.upb.soot.jimple.stmt.ReturnStmt;
import de.upb.soot.jimple.stmt.ReturnVoidStmt;
import de.upb.soot.jimple.stmt.TableSwitchStmt;
import de.upb.soot.jimple.stmt.ThrowStmt;

public interface StmtSwitch extends IVisitor
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
