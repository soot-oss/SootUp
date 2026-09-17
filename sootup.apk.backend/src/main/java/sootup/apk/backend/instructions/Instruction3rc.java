package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.backend.DexUtil;
import sootup.apk.backend.Register;

public class Instruction3rc extends AbstractInstruction {

  private final List<Register> registers;
  private final int registerSize;
  private final Reference reference;

  public Instruction3rc(
      Opcode opcode, List<Register> registers, int registerSize, Reference reference) {
    super(opcode, registers);
    this.registers = registers;
    this.registerSize = registerSize;
    this.reference = reference;
    logSmali();
  }

  public Reference getReference() {
    return reference;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction3rc(
        getOpcode(), registers.get(0).getNumber(), registerSize, reference);
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
    // if there is one problem -> all regs are incompatible (this could be optimized in reg
    // allocation, probably)
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
    // the only "hole" that is allowed: if regN is wide -> regN+1 must not be there
    Register startReg = registers.get(0);
    int nextExpectedRegNum = startReg.getNumber() + 1;
    if (startReg.isWide()) {
      nextExpectedRegNum++;
    }
    // loop starts at 1, since the first reg alone cannot have a hole
    for (int i = 1; i < registers.size(); i++) {
      Register r = registers.get(i);
      int regNum = r.getNumber();
      if (regNum != nextExpectedRegNum) {
        return true;
      }
      nextExpectedRegNum++;
      if (r.isWide()) {
        nextExpectedRegNum++;
      }
    }
    return false;
  }

  @Override
  public void logSmali() {
    String var1 = null;
    String var2 = null;
    if (reference instanceof MethodReference methodReference) {
      var1 = methodReference.getDefiningClass();
      var2 =
          methodReference.getName()
              + "("
              + String.join("", methodReference.getParameterTypes())
              + ")"
              + methodReference.getReturnType();
    } else if (reference instanceof TypeReference typeReference) {
      var1 = typeReference.getType();
      var2 = typeReference.getType();
    }

    log.info(
        String.format(
            "%s {%s}, %s->%s",
            getOpcode().name,
            "v"
                + registers.get(0).getNumber()
                + " .. "
                + "v"
                + (registers.get(0).getNumber() + registerSize - 1),
            var1,
            var2));
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}
}
