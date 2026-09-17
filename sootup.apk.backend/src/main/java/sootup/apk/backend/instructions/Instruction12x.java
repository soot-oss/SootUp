package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import sootup.apk.backend.Register;

public class Instruction12x extends TwoRegisterInstruction {

  public Instruction12x(Opcode opcode, Register registerA, Register registerB) {
    super(opcode, registerA, registerB);
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction12x(
        getOpcode(), getRegisterA().getNumber(), getRegisterB().getNumber());
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of(getRegisterA());
  }

  @Override
  public List<Register> getUseRegisters() {
    if (getOpcode().name.toLowerCase().contains("2addr")) {
      return getRegisters();
    }
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
        "{} v{} v{}", getOpcode().name, getRegisterA().getNumber(), getRegisterB().getNumber());
  }
}
