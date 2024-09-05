package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;

public class InvokeVirtualInstruction extends MethodInvocationInstruction {
  public InvokeVirtualInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    jimplifyVirtual(body);
  }
}
