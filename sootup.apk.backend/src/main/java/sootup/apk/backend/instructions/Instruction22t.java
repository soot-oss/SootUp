package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;
import sootup.apk.backend.Register;
import sootup.core.jimple.common.stmt.Stmt;

public class Instruction22t extends TwoRegisterInstruction {

  private final Stmt targetStmt;

  public Instruction22t(Opcode opcode, Register registerA, Register registerB, Stmt targetStmt) {
    super(opcode, registerA, registerB);
    this.targetStmt = targetStmt;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction22t(
        getOpcode(),
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        getLabelAssigner().getOrCreateLabel(targetStmt));
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{}, v{} :{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getRegisterB().getNumber(),
        "label");
  }

  public Stmt getTargetStmt() {
    return targetStmt;
  }
}
