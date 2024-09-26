package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction22t;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.stmt.JIfStmt;

public class IfTestInstruction extends ConditionalJumpInstruction {
  public IfTestInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  protected JIfStmt ifStatement(DexBody dexBody) {
    Instruction22t i = (Instruction22t) instruction;
    Local one = dexBody.getRegisterLocal(i.getRegisterA());
    Local other = dexBody.getRegisterLocal(i.getRegisterB());
    AbstractConditionExpr condition = getComparisonExpr(one, other);
    return Jimple.newIfStmt(condition, StmtPositionInfo.getNoStmtPositionInfo());
  }
}
