package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;

public class InvokeStaticInstruction extends MethodInvocationInstruction {
  @Override
  public void jimplify(DexBody body) {
    jimplifyStatic(body);
  }

  public InvokeStaticInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
