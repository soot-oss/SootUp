package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.parser.main.DexBody;

public class InvokeSpecialInstruction extends MethodInvocationInstruction {
  @Override
  public void jimplify(DexBody body) {
    jimplifySpecial(body);
  }

  public InvokeSpecialInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
