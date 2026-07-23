package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22c;
import org.jf.dexlib2.iface.reference.*;
import sootup.apk.backend.Register;

public class Instruction22c extends TwoRegisterInstruction {

  private final Reference reference;

  public Instruction22c(
      Opcode opcode, Register registerA, Register registerB, Reference reference) {
    super(opcode, registerA, registerB);
    this.reference = reference;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction22c(
        getOpcode(), getRegisterA().getNumber(), getRegisterB().getNumber(), reference);
  }

  @Override
  public void logSmali() {
    String value = null;
    if (reference instanceof FieldReference fieldReference) {
      value =
          fieldReference.getDefiningClass()
              + "->"
              + fieldReference.getName()
              + ":"
              + fieldReference.getType();
    } else if (reference instanceof TypeReference typeReference) {
      value = typeReference.getType();
    }

    log.info(
        "{} v{}, v{}, {}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        value);
  }
}
