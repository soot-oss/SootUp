package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class AbstractPayload extends AbstractInstruction {
  public AbstractPayload(Opcode opcode, List<Register> registers) {
    super(opcode, registers);
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of();
  }

  @Override
  public List<Register> getUseRegisters() {
    return List.of();
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}
}
