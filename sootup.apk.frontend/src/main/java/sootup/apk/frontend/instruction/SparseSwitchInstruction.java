package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

public class SparseSwitchInstruction extends SwitchInstruction {

  public SparseSwitchInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
    JSwitchStmt switchStmt =
        Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(switchStmt);
    return switchStmt;
  }
}
