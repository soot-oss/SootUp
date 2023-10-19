package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class MoveInstruction extends DexLibAbstractInstruction {

    @Override
    public void jimplify(DexBody body) {
        TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;

        int dest = i.getRegisterA();
        int source = i.getRegisterB();
        JAssignStmt jAssignStmt = Jimple.newAssignStmt(body.getRegisterLocal(dest), body.getRegisterLocal(source), StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(jAssignStmt);
        body.add(jAssignStmt);
    }

    @Override
    int movesRegister(int register) {
        TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
        int dest = i.getRegisterA();
        int source = i.getRegisterB();
        if (register == source) {
            return dest;
        }
        return -1;
    }

    @Override
    int movesToRegister(int register) {
        TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
        int dest = i.getRegisterA();
        int source = i.getRegisterB();
        if (register == dest) {
            return source;
        }
        return -1;
    }

    public MoveInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }
}
