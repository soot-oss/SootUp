package sootup.apk.backend.instructions;

import java.util.*;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.backend.Register;

public class Instruction35c extends FiveRegisterInstruction {

  private final int registerCount;
  private final Reference reference;

  public Instruction35c(
      Opcode opcode,
      int registerCount,
      Register registerA,
      Register registerB,
      Register registerC,
      Register registerD,
      Register registerE,
      Reference reference) {
    super(opcode, registerA, registerB, registerC, registerD, registerE);
    this.registerCount = registerCount;
    this.reference = reference;
    logSmali();
  }

  public Reference getReference() {
    return reference;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    List<Integer> registerNumbers = getInvokeRegisterNumbers();
    return new BuilderInstruction35c(
        getOpcode(),
        registerCount,
        registerNumbers.get(0),
        registerNumbers.get(1),
        registerNumbers.get(2),
        registerNumbers.get(3),
        registerNumbers.get(4),
        reference);
  }

  @Override
  public List<Register> getDefRegisters() {
    return List.of();
  }

  @Override
  public List<Register> getUseRegisters() {
    return getRegisters();
  }

  @Override
  public BitSet getIncompatibleRegs() {
    BitSet incompatRegs = new BitSet(5);
    for (int i = 0; i < getRegisters().size(); i++) {
      if (!getRegisters().get(i).fitsByte()) {
        incompatRegs.set(i);
      }
    }
    return incompatRegs;
  }

  @Override
  public void logSmali() {
    List<Register> registers =
        Stream.of(getRegisterA(), getRegisterB(), getRegisterC(), getRegisterD(), getRegisterE())
            .filter(Objects::nonNull)
            .toList();

    String var1;
    String var2;

    if (reference instanceof MethodReference m) {
      var1 = m.getDefiningClass();
      var2 = m.getName() + "(" + String.join("", m.getParameterTypes()) + ")" + m.getReturnType();
    } else if (reference instanceof TypeReference t) {
      var1 = var2 = t.getType();
    } else {
      var1 = var2 = null;
    }

    log.info(
        String.format(
            "%s {%s}, %s->%s",
            getOpcode().name,
            String.join(", ", registers.stream().map(r -> "v" + r.getNumber()).toList()),
            var1,
            var2));
  }
}
