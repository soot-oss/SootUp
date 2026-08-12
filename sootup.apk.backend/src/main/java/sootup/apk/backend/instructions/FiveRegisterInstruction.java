package sootup.apk.backend.instructions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import sootup.apk.backend.Register;

public abstract class FiveRegisterInstruction extends AbstractInstruction {

  private Register registerA;
  private Register registerB;
  private Register registerC;
  private Register registerD;
  private Register registerE;

  public FiveRegisterInstruction(
      Opcode opcode,
      Register registerA,
      Register registerB,
      Register registerC,
      Register registerD,
      Register registerE) {
    super(
        opcode,
        Stream.of(registerA, registerB, registerC, registerD, registerE)
            .filter(Objects::nonNull)
            .toList());
    this.registerA = registerA;
    this.registerB = registerB;
    this.registerC = registerC;
    this.registerD = registerD;
    this.registerE = registerE;
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

  protected Register getRegisterD() {
    return registerD;
  }

  protected Register getRegisterE() {
    return registerE;
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
    } else if (registerC != null && registerC.equals(oldRegister)) {
      registerC = newRegister;
    } else if (registerD != null && registerD.equals(oldRegister)) {
      registerD = newRegister;
    } else if (registerE != null && registerE.equals(oldRegister)) {
      registerE = newRegister;
    }
    super.setRegisters(List.copyOf(mutableList));
  }

  public List<Integer> getInvokeRegisterNumbers() {
    return Stream.concat(
            Stream.of(registerA, registerB, registerC, registerD, registerE)
                .filter(Objects::nonNull)
                .flatMap(
                    r ->
                        r.getSize() > 1
                            ? Stream.of(r.getNumber(), r.getNumber() + 1)
                            : Stream.of(r.getNumber())),
            Stream.generate(() -> 0))
        .limit(5)
        .collect(Collectors.toList());
  }
}
