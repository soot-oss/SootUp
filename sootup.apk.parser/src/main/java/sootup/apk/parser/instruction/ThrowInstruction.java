package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JThrowStmt;

public class ThrowInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    Instruction11x throwInstruction = (Instruction11x) instruction;
    JThrowStmt jThrowStmt =
        Jimple.newThrowStmt(
            body.getRegisterLocal(throwInstruction.getRegisterA()),
            StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jThrowStmt);
    body.add(jThrowStmt);
  }

  public ThrowInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
