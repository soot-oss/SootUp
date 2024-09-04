package sootup.apk.parser.instruction;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class Binop2addrInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException(
          "Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x binOp2AddrInstr = (Instruction12x) instruction;
    int dest = binOp2AddrInstr.getRegisterA();

    Local source1 = body.getRegisterLocal(binOp2AddrInstr.getRegisterA());
    Local source2 = body.getRegisterLocal(binOp2AddrInstr.getRegisterB());

    Value expr = getExpression(source1, source2);

    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), expr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  public Binop2addrInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private Value getExpression(Local source1, Local source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_INT_2ADDR:
      case ADD_LONG_2ADDR:
      case ADD_DOUBLE_2ADDR:
      case ADD_FLOAT_2ADDR:
        return Jimple.newAddExpr(source1, source2);
      case SUB_LONG_2ADDR:
      case SUB_DOUBLE_2ADDR:
      case SUB_FLOAT_2ADDR:
      case SUB_INT_2ADDR:
        return Jimple.newSubExpr(source1, source2);
      case MUL_LONG_2ADDR:
      case MUL_DOUBLE_2ADDR:
      case MUL_FLOAT_2ADDR:
      case MUL_INT_2ADDR:
        return Jimple.newMulExpr(source1, source2);
      case DIV_DOUBLE_2ADDR:
      case DIV_FLOAT_2ADDR:
      case DIV_INT_2ADDR:
      case DIV_LONG_2ADDR:
        return Jimple.newDivExpr(source1, source2);
      case REM_DOUBLE_2ADDR:
      case REM_FLOAT_2ADDR:
      case REM_INT_2ADDR:
      case REM_LONG_2ADDR:
        return Jimple.newRemExpr(source1, source2);
      case AND_LONG_2ADDR:
      case AND_INT_2ADDR:
        return Jimple.newAndExpr(source1, source2);
      case OR_INT_2ADDR:
      case OR_LONG_2ADDR:
        return Jimple.newOrExpr(source1, source2);
      case XOR_INT_2ADDR:
      case XOR_LONG_2ADDR:
        return Jimple.newXorExpr(source1, source2);
      case SHR_INT_2ADDR:
      case SHR_LONG_2ADDR:
        return Jimple.newShrExpr(source1, source2);
      case SHL_INT_2ADDR:
      case SHL_LONG_2ADDR:
        return Jimple.newShlExpr(source1, source2);
      case USHR_INT_2ADDR:
      case USHR_LONG_2ADDR:
        return Jimple.newUshrExpr(source1, source2);
      default:
        throw new RuntimeException("Invalid Opcode_2ADDR: " + opcode);
    }
  }
}
