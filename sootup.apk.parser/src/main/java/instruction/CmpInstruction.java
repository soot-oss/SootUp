package instruction;

import main.DexBody;
import main.TaggedInstruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.Expr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import tag.DoubleOpTag;
import tag.FloatOpTag;
import tag.LongOpTag;

public class CmpInstruction extends TaggedInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x cmpInstr = (Instruction23x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local first = body.getRegisterLocal(cmpInstr.getRegisterB());
    Local second = body.getRegisterLocal(cmpInstr.getRegisterC());

    // Expr cmpExpr;
    // Type type = null
    Opcode opcode = instruction.getOpcode();
    Expr cmpExpr = null;
    Type type = null;
    switch (opcode) {
      case CMPL_DOUBLE:
        setTag(new DoubleOpTag());
        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      case CMPL_FLOAT:
        setTag(new FloatOpTag());
        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      case CMPG_DOUBLE:
        setTag(new DoubleOpTag());
        type = PrimitiveType.DoubleType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMPG_FLOAT:
        setTag(new FloatOpTag());
        type = PrimitiveType.FloatType.getInstance();
        cmpExpr = Jimple.newCmpgExpr(first, second);
        break;
      case CMP_LONG:
        setTag(new LongOpTag());
        type = PrimitiveType.LongType.getInstance();
        cmpExpr = Jimple.newCmpExpr(first, second);
        break;
      default:
        throw new RuntimeException("no opcode for CMP: " + opcode);
    }

    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), cmpExpr, StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  public CmpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
