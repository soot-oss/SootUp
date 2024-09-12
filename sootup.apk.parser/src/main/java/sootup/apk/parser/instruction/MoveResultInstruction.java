package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class MoveResultInstruction extends DexLibAbstractInstruction {

  public MoveResultInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();

    JAssignStmt assignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest),
            body.getStoreResultLocal(),
            StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assignStmt);
    body.add(assignStmt);
  }
}
