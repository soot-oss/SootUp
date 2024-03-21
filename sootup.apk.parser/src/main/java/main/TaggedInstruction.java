package main;

import instruction.DexLibAbstractInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;

public abstract class TaggedInstruction extends DexLibAbstractInstruction {

  public TaggedInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  public void setTag(Tag t) {}
}
