package sootup.apk.backend.instructions;

import java.util.List;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10t;
import sootup.apk.backend.Register;
import sootup.core.jimple.common.stmt.Stmt;

public class Instruction10t extends AbstractInstruction {

  private final Stmt targetStmt;

  public Instruction10t(Opcode opcode, Stmt targetStmt) {
    super(opcode, List.of());
    this.targetStmt = targetStmt;
  }

  public Stmt getTargetStmt() {
    return targetStmt;
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    BuilderInstruction builderInstruction =
        new BuilderInstruction10t(
            super.getOpcode(), getLabelAssigner().getOrCreateLabel(targetStmt));
    logSmali();
    return builderInstruction;
  }

  @Override
  public void logSmali() {
    log.info("{} :{}", getOpcode().name, getLabelAssigner().getLabelName(targetStmt));
  }

  @Override
  public void changeRegister(Register oldRegister, Register newRegister) {}
}
