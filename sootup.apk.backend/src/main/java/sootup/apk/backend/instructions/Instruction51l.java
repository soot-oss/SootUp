package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction51l;
import sootup.apk.backend.Register;

public class Instruction51l extends OneRegisterInstruction {

  private final long value;

  public Instruction51l(Opcode opcode, Register register, long value) {
    super(opcode, register);
    this.value = value;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction51l(getOpcode(), getRegisterA().getNumber(), value);
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of(getRegisterA());
  }

  @Override
  public List<Register> getUseRegisters() {
    return List.of();
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, 0x{}L", getOpcode().name, getRegisterA().getNumber(), Long.toHexString(value));
  }
}
