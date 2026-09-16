package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21s;
import sootup.apk.backend.Register;

public class Instruction21s extends OneRegisterInstruction {

  private final int value;

  public Instruction21s(Opcode opcode, Register register, int value) {
    super(opcode, register);
    this.value = value;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction21s(getOpcode(), getRegisterA().getNumber(), value);
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
        "{} v{}, 0x{}{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        Long.toHexString(value),
        getOpcode().name.toLowerCase().contains("wide") ? "L" : "");
  }
}
