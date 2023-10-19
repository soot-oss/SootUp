package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import sootup.core.jimple.common.stmt.Stmt;

public abstract class JumpInstruction extends DexLibAbstractInstruction {
    /**
     * @param instruction the underlying dexlib instruction
     * @param codeAddress the bytecode address of this instruction
     */
    public JumpInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    protected DexLibAbstractInstruction targetInstruction;
    protected Stmt markerUnit;

    protected DexLibAbstractInstruction getTargetInstruction(DexBody body) {
        int offset = ((OffsetInstruction) instruction).getCodeOffset();
        int targetAddress = codeAddress + offset;
        targetInstruction = body.instructionAtAddress(targetAddress);
        return targetInstruction;
    }
}
