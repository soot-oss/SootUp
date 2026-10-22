package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction4rcc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import sootup.apk.backend.DexUtil;
import sootup.apk.backend.Register;

public class Instruction4rcc extends AbstractInstruction {

  private final List<Register> registers;
  private final int registerSize;
  private final Reference reference;
  private final Reference reference2;

  public Instruction4rcc(
      Opcode opcode,
      List<Register> registers,
      int registerSize,
      Reference reference,
      Reference reference2) {
    super(opcode, registers);
    this.registers = registers;
    this.registerSize = registerSize;
    this.reference = reference;
    this.reference2 = reference2;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction4rcc(
        getOpcode(), registers.get(0).getNumber(), registerSize, reference, reference2);
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of();
  }

  @Override
  public List<Register> getUseRegisters() {
    return registers;
  }

  @Override
  public BitSet getIncompatibleRegs() {
    int regCount = DexUtil.getRegisterSizeCount(registers);
    if (hasHoleInRange()) {
      return getAllIncompatible(regCount);
    }
    for (Register r : registers) {
      if (!r.fitsUnconstrained()) {
        return getAllIncompatible(regCount);
      }
      if (r.isWide()) {
        boolean secondWideHalfFits = Register.fitsUnconstrained(r.getNumber() + 1, false);
        if (!secondWideHalfFits) {
          return getAllIncompatible(regCount);
        }
      }
    }
    return new BitSet(regCount);
  }

  private static BitSet getAllIncompatible(int regCount) {
    BitSet incompatRegs = new BitSet(regCount);
    incompatRegs.flip(0, regCount);
    return incompatRegs;
  }

  private boolean hasHoleInRange() {
    int nextRegisterNumber = registers.get(0).getNumber() + registers.get(0).getSize();

    for (Register r : registers.subList(1, registers.size())) {
      if (r.getNumber() != nextRegisterNumber) return true;
      nextRegisterNumber += r.getSize();
    }
    return false;
  }

  @Override
  public void logSmali() {
    if (reference instanceof MethodReference methodReference) {
      log.info(
          String.format(
              "%s {%s}, %s->%s",
              getOpcode().name,
              "v"
                  + registers.get(0).getNumber()
                  + " .. "
                  + "v"
                  + (registers.get(0).getNumber() + registerSize - 1),
              methodReference.getDefiningClass(),
              methodReference.getName()
                  + "("
                  + String.join("", methodReference.getParameterTypes())
                  + ")"
                  + methodReference.getReturnType()));
    }
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}
}
