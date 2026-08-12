package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class TwoRegisterInstruction extends AbstractInstruction {
  private Register registerA;
  private Register registerB;

  public TwoRegisterInstruction(Opcode opcode, Register registerA, Register registerB) {
    super(opcode, List.of(registerA, registerB));
    this.registerA = registerA;
    this.registerB = registerB;
  }

  protected Register getRegisterA() {
    return registerA;
  }

  protected Register getRegisterB() {
    return registerB;
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {
    List<Register> mutableList = new ArrayList<>(super.getRegisters());
    int index = mutableList.indexOf(oldRegister);
    if (index != -1) {
      mutableList.set(index, newRegister);
    }
    if (registerA != null && registerA.equals(oldRegister)) {
      registerA = newRegister;
    } else if (registerB != null && registerB.equals(oldRegister)) {
      registerB = newRegister;
    }
    super.setRegisters(List.copyOf(mutableList));
  }
}
