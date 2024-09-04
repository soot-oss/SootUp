package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;

public class MonitorExitInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    int reg = ((OneRegisterInstruction) instruction).getRegisterA();
    Local object = body.getRegisterLocal(reg);
    JExitMonitorStmt exitMonitorStmt =
        Jimple.newExitMonitorStmt(object, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(exitMonitorStmt);
    body.add(exitMonitorStmt);
  }

  public MonitorExitInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
