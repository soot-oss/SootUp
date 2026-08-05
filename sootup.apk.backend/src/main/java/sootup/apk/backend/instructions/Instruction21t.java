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
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    BuilderInstruction builderInstruction =
        new BuilderInstruction21t(
            getOpcode(),
            getRegisterA().getNumber(),
            getLabelAssigner().getOrCreateLabel(targetStmt));
    logSmali();
    return builderInstruction;
  }

  @Override
  public void logSmali() {
    log.info(
        "{} v{} :{}",
        getOpcode().name,
        getRegisterA().getNumber(),
        getLabelAssigner().getLabelName(targetStmt));
  }

  public Stmt getTargetStmt() {
    return targetStmt;
  }
}
