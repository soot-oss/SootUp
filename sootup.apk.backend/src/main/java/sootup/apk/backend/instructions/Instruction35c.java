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
  public void logSmali() {
    List<Register> registers =
        Stream.of(getRegisterA(), getRegisterB(), getRegisterC(), getRegisterD(), getRegisterE())
            .filter(Objects::nonNull)
            .toList();
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
            String.join(", ", registers.stream().map(r -> "v" + r.getNumber()).toList()),
            var1,
            var2));
  }
}
