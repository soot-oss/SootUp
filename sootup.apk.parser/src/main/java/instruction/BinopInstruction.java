package instruction;

import main.DexBody;
import main.TaggedInstruction;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.JAssignStmt;
import tag.DoubleOpTag;
import tag.FloatOpTag;
import tag.IntOpTag;
import tag.LongOpTag;

public class BinopInstruction extends TaggedInstruction {
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
    JAssignStmt assign = Jimple.newAssignStmt(body.getRegisterLocal(dest), expr, null);
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
        setTag(new IntOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_LONG:
        setTag(new LongOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newAddExpr(source1, source2);
      case ADD_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newAddExpr(source1, source2);
      case SUB_LONG:
        setTag(new LongOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newSubExpr(source1, source2);
      case SUB_INT:
        setTag(new IntOpTag());
        return Jimple.newSubExpr(source1, source2);
      case MUL_LONG:
        setTag(new LongOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newMulExpr(source1, source2);
      case MUL_INT:
        setTag(new IntOpTag());
        return Jimple.newMulExpr(source1, source2);
      case DIV_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_INT:
        setTag(new IntOpTag());
        return Jimple.newDivExpr(source1, source2);
      case DIV_LONG:
        setTag(new LongOpTag());
        return Jimple.newDivExpr(source1, source2);
      case REM_DOUBLE:
        setTag(new DoubleOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_FLOAT:
        setTag(new FloatOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_INT:
        setTag(new IntOpTag());
        return Jimple.newRemExpr(source1, source2);
      case REM_LONG:
        setTag(new LongOpTag());
        return Jimple.newRemExpr(source1, source2);
      case AND_LONG:
        setTag(new LongOpTag());
        return Jimple.newAndExpr(source1, source2);
      case AND_INT:
        setTag(new IntOpTag());
        return Jimple.newAndExpr(source1, source2);
      case OR_INT:
        setTag(new IntOpTag());
        return Jimple.newOrExpr(source1, source2);
      case OR_LONG:
        setTag(new LongOpTag());
        return Jimple.newOrExpr(source1, source2);
      case XOR_INT:
        setTag(new IntOpTag());
        return Jimple.newXorExpr(source1, source2);
      case XOR_LONG:
        setTag(new LongOpTag());
        return Jimple.newXorExpr(source1, source2);
      case SHR_INT:
        setTag(new IntOpTag());
        return Jimple.newShrExpr(source1, source2);
      case SHR_LONG:
        setTag(new LongOpTag());
        return Jimple.newShrExpr(source1, source2);
      case SHL_INT:
        setTag(new IntOpTag());
        return Jimple.newShlExpr(source1, source2);
      case SHL_LONG:
        setTag(new LongOpTag());
        return Jimple.newShlExpr(source1, source2);
      case USHR_INT:
        setTag(new IntOpTag());
        return Jimple.newUshrExpr(source1, source2);
      case USHR_LONG:
        setTag(new LongOpTag());
        return Jimple.newUshrExpr(source1, source2);
      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }
}
