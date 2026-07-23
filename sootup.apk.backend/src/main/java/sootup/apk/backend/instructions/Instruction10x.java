package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import sootup.apk.backend.Register;

public class Instruction10x extends AbstractInstruction {

  public Instruction10x(Opcode opcode) {
    super(opcode, List.of());
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction10x(getOpcode());
  }

  @Override
  public void logSmali() {
    log.info(getOpcode().name);
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}
}
