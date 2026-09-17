package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22s;
import sootup.apk.backend.Register;

public class Instruction22s extends TwoRegisterInstruction {

  private final int literal;

  public Instruction22s(Opcode opcode, Register registerA, Register registerB, int literal) {
    super(opcode, registerA, registerB);
    this.literal = literal;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction22s(
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
    if (!getRegisterA().fitsByte()) result.set(0);
    if (!getRegisterB().fitsByte()) result.set(1);
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
