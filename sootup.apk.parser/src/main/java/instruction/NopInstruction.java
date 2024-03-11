package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;

public class NopInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    JNopStmt jNopStmt = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jNopStmt);
    // TODO: In one example, there comes NoOp Instruction after the return statement, dont know how to handle
    body.add(jNopStmt);
  }

  public NopInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
