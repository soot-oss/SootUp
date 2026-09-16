package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction32x;
import sootup.apk.backend.Register;

public class Instruction32x extends TwoRegisterInstruction {

  public Instruction32x(Opcode opcode, Register registerA, Register registerB) {
    super(opcode, registerA, registerB);
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction32x(
        getOpcode(), getRegisterA().getNumber(), getRegisterB().getNumber());
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
  public void logSmali() {
    log.info(
        "{} v{}, v{}", getOpcode().name, getRegisterA().getNumber(), getRegisterB().getNumber());
  }
}
