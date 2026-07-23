package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import sootup.apk.backend.Register;

public class Instruction11n extends OneRegisterInstruction {

  private final int value;

  public Instruction11n(Opcode opcode, Register register, int value) {
    super(opcode, register);
    this.value = value;
    logSmali();
  }

  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction11n(getOpcode(), getRegisterA().getNumber(), value);
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, 0x{}", getOpcode().name, getRegisterA().getNumber(), Integer.toHexString(value));
  }
}
