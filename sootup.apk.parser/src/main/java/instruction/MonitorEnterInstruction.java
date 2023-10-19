package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;

public class MonitorEnterInstruction extends DexLibAbstractInstruction {
    @Override
    public void jimplify(DexBody body) {
        int reg = ((OneRegisterInstruction) instruction).getRegisterA();
        Local object = body.getRegisterLocal(reg);
        JEnterMonitorStmt enterMonitorStmt = Jimple.newEnterMonitorStmt(object, StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(enterMonitorStmt);
        body.add(enterMonitorStmt);
    }

    public MonitorEnterInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }
}
