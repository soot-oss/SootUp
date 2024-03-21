package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JLengthExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class ArrayLengthInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException(
          "Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x lengthOfArrayInstruction = (Instruction12x) instruction;
    int dest = lengthOfArrayInstruction.getRegisterA();

    Local arrayReference = body.getRegisterLocal(lengthOfArrayInstruction.getRegisterB());

    JLengthExpr jLengthExpr = Jimple.newLengthExpr(arrayReference);
    JAssignStmt assignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jLengthExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assignStmt);
    body.add(assignStmt);
  }

  public ArrayLengthInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
