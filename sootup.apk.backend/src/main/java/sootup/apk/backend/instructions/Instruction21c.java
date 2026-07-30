package sootup.apk.backend.instructions;

import org.jf.dexlib2.MethodHandleType;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.reference.*;
import sootup.apk.backend.Register;

public class Instruction21c extends OneRegisterInstruction {

  private final Reference reference;

  public Instruction21c(Opcode opcode, Register registerA, Reference reference) {
    super(opcode, registerA);
    this.reference = reference;
    logSmali();
  }

  public Reference getReference() {
    return reference;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction21c(getOpcode(), getRegisterA().getNumber(), reference);
  }

  @Override
  public void logSmali() {
    String value = null;
    if (reference instanceof StringReference stringReference) {
      value = "\"" + stringReference.getString() + "\"";
    } else if (reference instanceof FieldReference fieldReference) {
      value =
          fieldReference.getDefiningClass() // Ljava/lang/System;
              + "->"
              + fieldReference.getName() // out
              + ":"
              + fieldReference.getType(); // Ljava/io/PrintStream; correct
    } else if (reference instanceof TypeReference typeReference) {
      value = typeReference.getType();
    } else if (reference instanceof MethodProtoReference methodProtoReference) {
      value =
          "("
              + String.join("", methodProtoReference.getParameterTypes())
              + ") "
              + methodProtoReference.getReturnType();
    } else if (reference instanceof MethodHandleReference methodHandleReference
        && methodHandleReference.getMemberReference() instanceof MethodReference methodReference) {
      value =
          MethodHandleType.toString(methodHandleReference.getMethodHandleType()).toLowerCase()
              + ", "
              + methodReference.getDefiningClass()
              + "->"
              + methodReference.getName()
              + "("
              + String.join("", methodReference.getParameterTypes())
              + ")"
              + methodReference.getReturnType();
    }

    log.info("{} v{}, {}", getOpcode().name, getRegisterA().getNumber(), value);
  }
}
