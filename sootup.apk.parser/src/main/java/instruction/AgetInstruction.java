package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.java.core.language.JavaJimple;

public class AgetInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x aGetInstr = (Instruction23x) instruction;
    int dest = aGetInstr.getRegisterA();

    Local arrayBase = body.getRegisterLocal(aGetInstr.getRegisterB());
    Local index = body.getRegisterLocal(aGetInstr.getRegisterC());

    JArrayRef jArrayRef = JavaJimple.getInstance().newArrayRef(arrayBase, index);
    Local l = body.getRegisterLocal(dest);

    JAssignStmt assignStmt =
        Jimple.newAssignStmt(l, jArrayRef, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assignStmt);
    body.add(assignStmt);
  }

  public AgetInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

}
