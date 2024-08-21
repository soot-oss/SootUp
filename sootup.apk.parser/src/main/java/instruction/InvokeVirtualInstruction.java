package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

public class InvokeVirtualInstruction extends MethodInvocationInstruction {
  public InvokeVirtualInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    jimplifyVirtual(body);
  }
}
