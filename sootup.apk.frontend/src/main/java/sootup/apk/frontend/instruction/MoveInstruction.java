package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class MoveInstruction extends DexLibAbstractInstruction {

  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;

    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest),
            body.getRegisterLocal(source),
            StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  @Override
  int movesToRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    if (register == dest) {
      return source;
    }
    return -1;
  }

  public MoveInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
