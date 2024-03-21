package instruction;

import main.DexBody;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.stmt.*;

public abstract class ConditionalJumpInstruction extends JumpInstruction
    implements DeferableInstruction {
  /**
   * @param instruction the underlying dexlib instruction
   * @param codeAddress the bytecode address of this instruction
   */
  public ConditionalJumpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  protected abstract JIfStmt ifStatement(DexBody dexBody);

  @Override
  public void jimplify(DexBody body) {
    // check if target instruction has been jimplified
    DexLibAbstractInstruction ins = getTargetInstruction(body);
    if (ins != null && ins.getStmt() != null) {
      JIfStmt s = ifStatement(body);
      body.add(s);
      setStmt(s);
    } else {
      // set marker unit to swap real gotostmt with otherwise
      body.addDeferredJimplification(this);
      markerUnit = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
      setStmt(markerUnit);
      body.add(stmt);
    }
  }

  protected AbstractConditionExpr getComparisonExpr(DexBody body, int reg) {
    Local one = body.getRegisterLocal(reg);
    return getComparisonExpr(one, IntConstant.getInstance(0));
  }

  /**
   * Get comparison expression depending on opcode between two immediates
   *
   * @param one first immediate
   * @param other second immediate
   * @throws RuntimeException if this is not a IfTest or IfTestz instruction.
   */
  protected AbstractConditionExpr getComparisonExpr(Immediate one, Immediate other) {
    Opcode opcode = instruction.getOpcode();

    switch (opcode) {
      case IF_EQ:
      case IF_EQZ:
        return Jimple.newEqExpr(one, other);
      case IF_NE:
      case IF_NEZ:
        return Jimple.newNeExpr(one, other);
      case IF_LT:
      case IF_LTZ:
        return Jimple.newLtExpr(one, other);
      case IF_GE:
      case IF_GEZ:
        return Jimple.newGeExpr(one, other);
      case IF_GT:
      case IF_GTZ:
        return Jimple.newGtExpr(one, other);
      case IF_LE:
      case IF_LEZ:
        return Jimple.newLeExpr(one, other);
      default:
        throw new RuntimeException("Instruction is not an IfTest(z) instruction.");
    }
  }

  @Override
  public void deferredJimplify(DexBody body) {
    JIfStmt jIfStmt = ifStatement(body);
    body.replaceStmt(markerUnit, jIfStmt);
    setStmt(jIfStmt);
  }
}
