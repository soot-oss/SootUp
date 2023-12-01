package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JGotoStmt;

public class GotoInstruction extends JumpInstruction implements DeferableInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (getTargetInstruction(body).getStmt() != null) {
      JGotoStmt jGotoStmt = gotoStatement();
      body.add(jGotoStmt);
      return;
    }
    body.addDeferredJimplification(this);
    markerUnit = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(markerUnit);
    body.add(markerUnit);
  }

  public GotoInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private JGotoStmt gotoStatement() {
    JGotoStmt go = Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(go);
    return go;
  }

  @Override
  public void deferredJimplify(DexBody body) {
    JGotoStmt jGotoStmt = gotoStatement();
    body.insertAfter(jGotoStmt, markerUnit);
    setStmt(jGotoStmt);
  }
}
