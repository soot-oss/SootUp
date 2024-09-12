package sootup.apk.parser.instruction;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class BinopInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }
    Instruction23x binOpInstr = (Instruction23x) instruction;
    int dest = binOpInstr.getRegisterA();

    Local source1 = body.getRegisterLocal(binOpInstr.getRegisterB());
    Local source2 = body.getRegisterLocal(binOpInstr.getRegisterC());

    Value expr = getExpression(source1, source2);
    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), expr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  public BinopInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  private Value getExpression(Local source1, Local source2) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case ADD_INT:
        return Jimple.newAddExpr(source1, source2);
      case ADD_LONG:
        return Jimple.newAddExpr(source1, source2);
      case ADD_DOUBLE:
        return Jimple.newAddExpr(source1, source2);
      case ADD_FLOAT:
        return Jimple.newAddExpr(source1, source2);
      case SUB_LONG:
        return Jimple.newSubExpr(source1, source2);
      case SUB_DOUBLE:
        return Jimple.newSubExpr(source1, source2);
      case SUB_FLOAT:
        return Jimple.newSubExpr(source1, source2);
      case SUB_INT:
        return Jimple.newSubExpr(source1, source2);
      case MUL_LONG:
      case MUL_FLOAT:
        return Jimple.newMulExpr(source1, source2);
      case MUL_DOUBLE:
        return Jimple.newMulExpr(source1, source2);
      case MUL_INT:
        return Jimple.newMulExpr(source1, source2);
      case DIV_DOUBLE:
        return Jimple.newDivExpr(source1, source2);
      case DIV_FLOAT:
        return Jimple.newDivExpr(source1, source2);
      case DIV_INT:
        return Jimple.newDivExpr(source1, source2);
      case DIV_LONG:
        return Jimple.newDivExpr(source1, source2);
      case REM_DOUBLE:
        return Jimple.newRemExpr(source1, source2);
      case REM_FLOAT:
        return Jimple.newRemExpr(source1, source2);
      case REM_INT:
        return Jimple.newRemExpr(source1, source2);
      case REM_LONG:
        return Jimple.newRemExpr(source1, source2);
      case AND_LONG:
        return Jimple.newAndExpr(source1, source2);
      case AND_INT:
        return Jimple.newAndExpr(source1, source2);
      case OR_INT:
        return Jimple.newOrExpr(source1, source2);
      case OR_LONG:
        return Jimple.newOrExpr(source1, source2);
      case XOR_INT:
        return Jimple.newXorExpr(source1, source2);
      case XOR_LONG:
        return Jimple.newXorExpr(source1, source2);
      case SHR_INT:
        return Jimple.newShrExpr(source1, source2);
      case SHR_LONG:
        return Jimple.newShrExpr(source1, source2);
      case SHL_INT:
        return Jimple.newShlExpr(source1, source2);
      case SHL_LONG:
        return Jimple.newShlExpr(source1, source2);
      case USHR_INT:
        return Jimple.newUshrExpr(source1, source2);
      case USHR_LONG:
        return Jimple.newUshrExpr(source1, source2);
      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }
}
