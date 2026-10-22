package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class OneRegisterInstruction extends AbstractInstruction {

  private Register registerA;

  public Register getRegisterA() {
    return registerA;
  }

  public OneRegisterInstruction(Opcode opcode, Register registerA) {
    super(opcode, List.of(registerA));
    this.registerA = registerA;
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {
    List<Register> mutableList = new ArrayList<>(super.getRegisters());
    mutableList.replaceAll(r -> r.equals(oldRegister) ? newRegister : r);
    if (Objects.equals(registerA, oldRegister)) registerA = newRegister;
    super.setRegisters(List.copyOf(mutableList));
  }
}
