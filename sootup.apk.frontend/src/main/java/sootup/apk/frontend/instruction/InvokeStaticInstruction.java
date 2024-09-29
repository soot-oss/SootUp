package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;

public class InvokeStaticInstruction extends MethodInvocationInstruction {
  @Override
  public void jimplify(DexBody body) {
    jimplifyStatic(body);
  }

  public InvokeStaticInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
