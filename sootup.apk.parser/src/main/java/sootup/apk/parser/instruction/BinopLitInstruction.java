package sootup.apk.parser.instruction;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction22b;
import org.jf.dexlib2.iface.instruction.formats.Instruction22s;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class BinopLitInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction22s) && !(instruction instanceof Instruction22b)) {
      throw new IllegalArgumentException(
          "Expected Instruction22s or Instruction22b but got: " + instruction.getClass());
    }

    NarrowLiteralInstruction binOpLitInstr = (NarrowLiteralInstruction) this.instruction;
    int dest = ((TwoRegisterInstruction) instruction).getRegisterA();
    int source = ((TwoRegisterInstruction) instruction).getRegisterB();

    Local source1 = body.getRegisterLocal(source);

    IntConstant constant = IntConstant.getInstance(binOpLitInstr.getNarrowLiteral());

    Value expr = getExpression(source1, constant);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), expr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public BinopLitInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private Value getExpression(Local source1, IntConstant source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_INT_LIT16:
      case ADD_INT_LIT8:
        return Jimple.newAddExpr(source1, source2);

      case RSUB_INT:
      case RSUB_INT_LIT8:
        return Jimple.newSubExpr(source2, source1);

      case MUL_INT_LIT16:
      case MUL_INT_LIT8:
        return Jimple.newMulExpr(source1, source2);

      case DIV_INT_LIT16:
      case DIV_INT_LIT8:
        return Jimple.newDivExpr(source1, source2);

      case REM_INT_LIT16:
      case REM_INT_LIT8:
        return Jimple.newRemExpr(source1, source2);

      case AND_INT_LIT8:
      case AND_INT_LIT16:
        return Jimple.newAndExpr(source1, source2);

      case OR_INT_LIT16:
      case OR_INT_LIT8:
        return Jimple.newOrExpr(source1, source2);

      case XOR_INT_LIT16:
      case XOR_INT_LIT8:
        return Jimple.newXorExpr(source1, source2);

      case SHL_INT_LIT8:
        return Jimple.newShlExpr(source1, source2);

      case SHR_INT_LIT8:
        return Jimple.newShrExpr(source1, source2);

      case USHR_INT_LIT8:
        return Jimple.newUshrExpr(source1, source2);

      default:
        throw new IllegalStateException("Invalid Opcode: " + opcode);
    }
  }
}
