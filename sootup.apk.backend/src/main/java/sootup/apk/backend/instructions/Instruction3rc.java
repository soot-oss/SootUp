package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.TypeReference;
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
