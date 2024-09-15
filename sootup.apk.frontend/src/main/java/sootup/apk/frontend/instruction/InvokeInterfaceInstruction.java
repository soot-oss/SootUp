package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;

public class InvokeInterfaceInstruction extends MethodInvocationInstruction {
  @Override
  public void jimplify(DexBody body) {
    jimplifyInterface(body);
  }

  public InvokeInterfaceInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
