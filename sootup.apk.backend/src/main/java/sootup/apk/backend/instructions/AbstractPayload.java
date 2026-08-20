package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class AbstractPayload extends AbstractInstruction {
  public AbstractPayload(Opcode opcode, List<Register> registers) {
    super(opcode, registers);
  }
}
