package main;

import instruction.DexLibAbstractInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;

public abstract class TaggedInstruction extends DexLibAbstractInstruction {

    private Tag instructionTag = null;

    public TaggedInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    public void setTag(Tag t) {
        instructionTag = t;
    }

    public Tag getTag() {
        if (instructionTag == null) {
            throw new RuntimeException(
                    "Must tag instruction first! (0x" + Integer.toHexString(codeAddress) + ": " + instruction + ")");
        }
        return instructionTag;
    }
}
