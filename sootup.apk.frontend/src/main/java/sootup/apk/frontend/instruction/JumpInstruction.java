package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OffsetInstruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.common.stmt.Stmt;

public abstract class JumpInstruction extends DexLibAbstractInstruction {
  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public JumpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  public DexLibAbstractInstruction targetInstruction;
  protected Stmt markerUnit;

  protected DexLibAbstractInstruction getTargetInstruction(DexBody body) {
    int offset = ((OffsetInstruction) instruction).getCodeOffset();
    int targetAddress = codeAddress + offset;
    targetInstruction = body.instructionAtAddress(targetAddress);
    return targetInstruction;
  }
}
