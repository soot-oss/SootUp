package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

public class PackedSwitchInstruction extends SwitchInstruction {

  public PackedSwitchInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
    JSwitchStmt jSwitchStmt =
        Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jSwitchStmt);
    return jSwitchStmt;
  }
}
