package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class ThreeRegisterInstruction extends AbstractInstruction {
  private Register registerA;
  private Register registerB;
  private Register registerC;

  public ThreeRegisterInstruction(
      Opcode opcode, Register registerA, Register registerB, Register registerC) {
    super(opcode, List.of(registerA, registerB, registerC));
    this.registerA = registerA;
    this.registerB = registerB;
    this.registerC = registerC;
  }

  protected Register getRegisterA() {
    return registerA;
  }

  protected Register getRegisterB() {
    return registerB;
  }

  protected Register getRegisterC() {
    return registerC;
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {
    List<Register> mutableList = new ArrayList<>(super.getRegisters());
    int index = mutableList.indexOf(oldRegister);
    if (index != -1) mutableList.set(index, newRegister);

    if (Objects.equals(registerA, oldRegister)) registerA = newRegister;
    else if (Objects.equals(registerB, oldRegister)) registerB = newRegister;
    else if (Objects.equals(registerC, oldRegister)) registerC = newRegister;

    super.setRegisters(List.copyOf(mutableList));
  }
}
