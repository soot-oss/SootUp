package sootup.apk.backend.instructions;

import java.util.BitSet;
import java.util.List;
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
  }

  @Override
  public BuilderInstruction getBuilderInstruction() {
    BuilderInstruction builderInstruction =
        new BuilderInstruction22t(
            getOpcode(),
            getRegisterA().getNumber(),
            getRegisterB().getNumber(),
            getLabelAssigner().getOrCreateLabel(targetStmt));
    logSmali();
    return builderInstruction;
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
    BitSet result = new BitSet(2);
    if (!getRegisterA().fitsByte()) result.set(0);
    if (!getRegisterB().fitsByte()) result.set(1);
    return result;
  }

  @Override
  public void logSmali() {
    if (getLabelAssigner() != null) {
      log.info(
          "{} v{}, v{} :{}",
          getOpcode().name,
          getRegisterA().getNumber(),
          getRegisterB().getNumber(),
          getLabelAssigner().getLabelName(targetStmt));
    } else {
      log.info(
          "{} v{}, v{} :label",
          getOpcode().name,
          getRegisterA().getNumber(),
          getRegisterB().getNumber());
    }
  }

  public Stmt getTargetStmt() {
    return targetStmt;
  }
}
