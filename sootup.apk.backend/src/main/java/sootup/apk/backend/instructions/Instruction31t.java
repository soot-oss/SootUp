package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31t;
import sootup.apk.backend.Register;

public class Instruction31t extends OneRegisterInstruction {

  AbstractPayload payload;

  public Instruction31t(Opcode opcode, Register registerA, AbstractPayload payload) {
    super(opcode, registerA);
    this.payload = payload;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    BuilderInstruction builderInstruction =
        new BuilderInstruction31t(
            super.getOpcode(),
            getRegisterA().getNumber(),
            getLabelAssigner().getOrCreateLabel(payload));
    logSmali();
    return builderInstruction;
  }

  @Override
  public void logSmali() {
    log.info(
        "{} {}, :{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getLabelAssigner().getLabelName(payload));
  }
}
