package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class OneRegisterInstruction extends AbstractInstruction {

  private Register registerA;

  protected Register getRegisterA() {
    return registerA;
  }

  public OneRegisterInstruction(Opcode opcode, Register registerA) {
    super(opcode, List.of(registerA));
    this.registerA = registerA;
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {
    List<Register> mutableList = new ArrayList<>(super.getRegisters());
    int index = mutableList.indexOf(oldRegister);
    if (index != -1) {
      mutableList.set(index, newRegister);
    }
    if (registerA.equals(oldRegister)) {
      registerA = newRegister;
    }
    super.setRegisters(List.copyOf(mutableList));
  }
}
