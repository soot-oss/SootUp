package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

public class InvokeSpecialInstruction extends MethodInvocationInstruction {
    @Override
    public void jimplify(DexBody body) {
        jimplifySpecial(body);
    }

    public InvokeSpecialInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }
}
