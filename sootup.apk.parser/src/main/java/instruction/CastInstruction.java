package instruction;

import main.DexBody;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public class CastInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    Type targetType = getTargetType();
    JCastExpr jCastExpr = Jimple.newCastExpr(body.getRegisterLocal(source), targetType);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jCastExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  private Type getTargetType() {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case INT_TO_BYTE:
        return PrimitiveType.ByteType.getInstance();
      case INT_TO_CHAR:
        return PrimitiveType.CharType.getInstance();
      case INT_TO_SHORT:
        return PrimitiveType.ShortType.getInstance();

      case LONG_TO_INT:
      case FLOAT_TO_INT:
      case DOUBLE_TO_INT:
        return PrimitiveType.IntType.getInstance();

      case INT_TO_LONG:
      case FLOAT_TO_LONG:
      case DOUBLE_TO_LONG:
        return PrimitiveType.LongType.getInstance();

      case LONG_TO_FLOAT:
      case INT_TO_FLOAT:
      case DOUBLE_TO_FLOAT:
        return PrimitiveType.FloatType.getInstance();

      case INT_TO_DOUBLE:
      case LONG_TO_DOUBLE:
      case FLOAT_TO_DOUBLE:
        return PrimitiveType.DoubleType.getInstance();

      default:
        throw new IllegalStateException("Invalid Opcode: " + opcode);
    }
  }

  public CastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
