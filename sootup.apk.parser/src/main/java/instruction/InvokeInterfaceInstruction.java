package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

public class InvokeInterfaceInstruction extends MethodInvocationInstruction {
  @Override
  public void jimplify(DexBody body) {
    jimplifyInterface(body);
  }

  public InvokeInterfaceInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
