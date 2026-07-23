package sootup.apk.backend.instructions;

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
  public void logSmali() {
    log.info("{} v{}", getOpcode().name, getRegisterA().getNumber());
  }
}
