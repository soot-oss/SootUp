package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import sootup.apk.backend.Register;

public class Instruction11x extends OneRegisterInstruction {

  public Instruction11x(Opcode opcode, Register registerA) {
    super(opcode, registerA);
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction11x(getOpcode(), getRegisterA().getNumber());
  }

  @Override
  public List<Register> getDefRegisters() {
    if (getOpcode().name.toLowerCase().startsWith("move")) {
      return List.of(getRegisterA());
    } else {
      return List.of();
    }
  }

  @Override
  public List<Register> getUseRegisters() {
    if (!getOpcode().name.toLowerCase().startsWith("move")) {
      return List.of(getRegisterA());
    } else {
      return List.of();
    }
  }

  @Override
  public BitSet getIncompatibleRegs() {
    BitSet result = new BitSet(1);
    if (!getRegisterA().fitsShort()) result.set(0);
    return result;
  }

  @Override
  public void logSmali() {
    log.info("{} v{}", getOpcode().name, getRegisterA().getNumber());
  }
}
