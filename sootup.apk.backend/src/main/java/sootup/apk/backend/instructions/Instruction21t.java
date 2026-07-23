package sootup.apk.backend.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21t;
import sootup.apk.backend.Register;
import sootup.core.jimple.common.stmt.Stmt;

public class Instruction21t extends OneRegisterInstruction {

  private final Stmt targetStmt;

  public Instruction21t(Opcode opcode, Register registerA, Stmt targetStmt) {
    super(opcode, registerA);
    this.targetStmt = targetStmt;
    logSmali();
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    logSmali();
    return new BuilderInstruction21t(
        getOpcode(), getRegisterA().getNumber(), getLabelAssigner().getOrCreateLabel(targetStmt));
  }

  @Override
  public void logSmali() {
    log.info("{} v{} :{}", getOpcode().name, getRegisterA().getNumber(), "label");
  }

  public Stmt getTargetStmt() {
    return targetStmt;
  }
}
