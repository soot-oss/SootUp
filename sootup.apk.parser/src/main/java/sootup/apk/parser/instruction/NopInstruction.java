package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;

public class NopInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    JNopStmt jNopStmt = Jimple.newNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jNopStmt);
    body.add(jNopStmt);
  }

  public NopInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
