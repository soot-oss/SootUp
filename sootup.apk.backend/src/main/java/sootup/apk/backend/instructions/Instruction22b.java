package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22b;
import sootup.apk.backend.Register;

public class Instruction22b extends TwoRegisterInstruction {

  private final int literal;

  public Instruction22b(Opcode opcode, Register registerA, Register registerB, int literal) {
    super(opcode, registerA, registerB);
    this.literal = literal;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction22b(
        getOpcode(), getRegisterA().getNumber(), getRegisterB().getNumber(), literal);
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of(getRegisterA());
  }

  @Override
  public List<Register> getUseRegisters() {
    return List.of(getRegisterB());
  }

  @Override
  public BitSet getIncompatibleRegs() {
    BitSet result = new BitSet(2);
    if (!getRegisterA().fitsShort()) result.set(0);
    if (!getRegisterB().fitsShort()) result.set(1);
    return result;
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, v{} {}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        literal);
  }
}
