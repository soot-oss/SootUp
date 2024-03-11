package instruction;

import main.DexBody;
import main.TaggedInstruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import tag.DoubleOpTag;
import tag.FloatOpTag;
import tag.IntOpTag;
import tag.LongOpTag;

public class CastInstruction extends TaggedInstruction {
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
        setTag(new IntOpTag());
        return PrimitiveType.ByteType.getInstance();
      case INT_TO_CHAR:
        setTag(new IntOpTag());
        return PrimitiveType.CharType.getInstance();
      case INT_TO_SHORT:
        setTag(new IntOpTag());
        return PrimitiveType.ShortType.getInstance();

      case LONG_TO_INT:
        setTag(new LongOpTag());
        return PrimitiveType.IntType.getInstance();
      case DOUBLE_TO_INT:
        setTag(new DoubleOpTag());
        return PrimitiveType.IntType.getInstance();
      case FLOAT_TO_INT:
        setTag(new FloatOpTag());
        return PrimitiveType.IntType.getInstance();

      case INT_TO_LONG:
        setTag(new IntOpTag());
        return PrimitiveType.LongType.getInstance();
      case DOUBLE_TO_LONG:
        setTag(new DoubleOpTag());
        return PrimitiveType.LongType.getInstance();
      case FLOAT_TO_LONG:
        setTag(new FloatOpTag());
        return PrimitiveType.LongType.getInstance();

      case LONG_TO_FLOAT:
        setTag(new LongOpTag());
        return PrimitiveType.FloatType.getInstance();
      case DOUBLE_TO_FLOAT:
        setTag(new DoubleOpTag());
        return PrimitiveType.FloatType.getInstance();
      case INT_TO_FLOAT:
        setTag(new IntOpTag());
        return PrimitiveType.FloatType.getInstance();

      case INT_TO_DOUBLE:
        setTag(new IntOpTag());
        return PrimitiveType.DoubleType.getInstance();
      case FLOAT_TO_DOUBLE:
        setTag(new FloatOpTag());
        return PrimitiveType.DoubleType.getInstance();
      case LONG_TO_DOUBLE:
        setTag(new LongOpTag());
        return PrimitiveType.DoubleType.getInstance();

      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  public CastInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
