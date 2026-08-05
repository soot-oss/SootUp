package sootup.apk.backend.instructions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction45cc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import sootup.apk.backend.Register;

public class Instruction45cc extends FiveRegisterInstruction {

  private final int registerCount;
  private final Reference reference;
  private final Reference reference2;

  public Instruction45cc(
      Opcode opcode,
      int registerCount,
      Register registerA,
      Register registerB,
      Register registerC,
      Register registerD,
      Register registerE,
      Reference reference,
      Reference reference2) {
    super(opcode, registerA, registerB, registerC, registerD, registerE);
    this.registerCount = registerCount;
    this.reference = reference;
    this.reference2 = reference2;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    List<Integer> registerNumbers = getInvokeRegisterNumbers();
    return new BuilderInstruction45cc(
        getOpcode(),
        registerCount,
        registerNumbers.get(0),
        registerNumbers.get(1),
        registerNumbers.get(2),
        registerNumbers.get(3),
        registerNumbers.get(4),
        reference,
        reference2);
  }

  @Override
  public void logSmali() {
    if (reference instanceof MethodReference methodReference) {
      List<Register> registers =
          Stream.of(getRegisterA(), getRegisterB(), getRegisterC(), getRegisterD(), getRegisterE())
              .filter(Objects::nonNull)
              .toList();
      log.info(
          String.format(
              "%s {%s}, %s->%s",
              getOpcode().name,
              String.join(", ", registers.stream().map(r -> "v" + r.getNumber()).toList()),
              methodReference.getDefiningClass(),
              methodReference.getName()
                  + "("
                  + String.join("", methodReference.getParameterTypes())
                  + ")"
                  + methodReference.getReturnType()));
    }
  }
}
