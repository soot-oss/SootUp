package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;

public class ReturnVoidInstruction extends DexLibAbstractInstruction {

  public ReturnVoidInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  public void jimplify(DexBody body) {
    JReturnVoidStmt returnStmt = Jimple.newReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(returnStmt);
    body.add(returnStmt);
  }
}
