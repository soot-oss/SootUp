package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction23x;
import sootup.apk.backend.Register;

public class Instruction23x extends ThreeRegisterInstruction {

  public Instruction23x(Opcode opcode, Register registerA, Register registerB, Register registerC) {
    super(opcode, registerA, registerB, registerC);
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction23x(
        getOpcode(),
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        getRegisterC().getNumber());
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, v{}, v{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        getRegisterC().getNumber());
  }
}
