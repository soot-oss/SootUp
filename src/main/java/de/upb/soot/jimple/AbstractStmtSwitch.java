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

public abstract class AbstractStmtSwitch implements StmtSwitch
{
    Object result;

    @Override
    public void caseBreakpointStmt(BreakpointStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseInvokeStmt(InvokeStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseAssignStmt(AssignStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseIdentityStmt(IdentityStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseEnterMonitorStmt(EnterMonitorStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseExitMonitorStmt(ExitMonitorStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseGotoStmt(GotoStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseIfStmt(IfStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseLookupSwitchStmt(LookupSwitchStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseNopStmt(NopStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseRetStmt(RetStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseReturnStmt(ReturnStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseReturnVoidStmt(ReturnVoidStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseTableSwitchStmt(TableSwitchStmt stmt)
    {
        defaultCase(stmt);
    }

    @Override
    public void caseThrowStmt(ThrowStmt stmt)
    {
        defaultCase(stmt);
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

