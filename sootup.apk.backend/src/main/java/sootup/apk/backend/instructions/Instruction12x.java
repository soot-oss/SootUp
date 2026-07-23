package sootup.apk.backend.instructions;

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
    log.info("{}", getRegisterA().getNumber());
    log.info("{}", getRegisterB().getNumber());
    return new BuilderInstruction12x(
        getOpcode(), getRegisterA().getNumber(), getRegisterB().getNumber());
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{} v{}", getOpcode().name, getRegisterA().getNumber(), getRegisterB().getNumber());
  }
}
