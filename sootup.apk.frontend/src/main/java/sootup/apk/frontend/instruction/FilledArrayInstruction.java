package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;

public abstract class FilledArrayInstruction extends DexLibAbstractInstruction
    implements DanglingInstruction {
  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public FilledArrayInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
