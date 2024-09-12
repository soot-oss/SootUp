package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.parser.Util.DexUtil;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.Type;

public class CheckCastInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction21c)) {
      throw new IllegalArgumentException(
          "Expected Instruction21c but got: " + instruction.getClass());
    }

    Instruction21c checkCastInstr = (Instruction21c) instruction;

    Local castValue = body.getRegisterLocal(checkCastInstr.getRegisterA());
    Type checkCastType =
        DexUtil.toSootType(((TypeReference) checkCastInstr.getReference()).getType(), 0);

    JCastExpr castExpr = Jimple.newCastExpr(castValue, checkCastType);
    // generate "x = (Type) x"
    // splitter will take care of the rest
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(castValue, castExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public CheckCastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
