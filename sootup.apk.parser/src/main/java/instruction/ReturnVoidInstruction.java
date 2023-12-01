package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;

public class ReturnVoidInstruction extends DexLibAbstractInstruction {

  public ReturnVoidInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  public void jimplify(DexBody body) {
    JReturnVoidStmt returnStmt = Jimple.newReturnVoidStmt(null);
    setStmt(returnStmt);
    body.add(returnStmt);
  }
}
