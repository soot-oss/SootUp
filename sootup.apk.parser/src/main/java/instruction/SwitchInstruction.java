package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;

public abstract class SwitchInstruction extends PseudoInstruction implements DeferableInstruction{

    protected Stmt markerUnit;
    /**
     * @param instruction the underlying dexlib instruction
     * @param codeAddress the bytecode address of this instruction
     */
    public SwitchInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    protected abstract Stmt switchStatement(DexBody body, Instruction targetData, Local key);

    @Override
    public void jimplify(DexBody body) {
        markerUnit = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(markerUnit);
        body.add(markerUnit);
        body.addDeferredJimplification(this);
    }

    @Override
    public void deferredJimplify(DexBody body) {
        int keyRegister = ((OneRegisterInstruction) instruction).getRegisterA();
        int offset = ((OffsetInstruction) instruction).getCodeOffset();
        Local key = body.getRegisterLocal(keyRegister);
        int targetAddress = codeAddress + offset;
        Instruction targetData = body.instructionAtAddress(targetAddress).instruction;
        Stmt stmt = switchStatement(body, targetData, key);
        body.insertAfter(stmt, markerUnit);
        setStmt(stmt);
    }
}
