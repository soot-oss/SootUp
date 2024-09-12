package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction11x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JReturnStmt;

public class ReturnInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    Instruction11x returnInstruction = (Instruction11x) this.instruction;
    Local l = body.getRegisterLocal(returnInstruction.getRegisterA());
    JReturnStmt jReturnStmt = Jimple.newReturnStmt(l, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jReturnStmt);
    body.add(jReturnStmt);
  }

  public ReturnInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
