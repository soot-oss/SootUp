package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.backend.Register;

public class Instruction31i extends OneRegisterInstruction {

  private static final Logger log = LoggerFactory.getLogger(Instruction31i.class);

  private final int value;

  public Instruction31i(Opcode opcode, Register register, int value) {
    super(opcode, register);
    this.value = value;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction31i(getOpcode(), getRegisterA().getNumber(), value);
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, 0x{}{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        Long.toHexString(value),
        getOpcode().name.toLowerCase().contains("wide") ? "L" : "");
  }
}
