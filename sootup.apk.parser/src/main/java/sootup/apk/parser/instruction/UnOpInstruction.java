package sootup.apk.parser.instruction;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction12x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class UnOpInstruction extends DexLibAbstractInstruction {
  public UnOpInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction12x)) {
      throw new IllegalArgumentException(
          "Expected Instruction12x but got: " + instruction.getClass());
    }

    Instruction12x cmpInstr = (Instruction12x) instruction;
    int dest = cmpInstr.getRegisterA();

    Local source = body.getRegisterLocal(cmpInstr.getRegisterB());
    Value expr = getExpression(source);
    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), expr, StmtPositionInfo.getNoStmtPositionInfo());

    setStmt(assign);
    body.add(assign);
  }

  /** Return the appropriate Jimple Expression according to the OpCode */
  private Value getExpression(Local source) {
    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case NEG_INT:
      case NEG_DOUBLE:
      case NEG_FLOAT:
      case NEG_LONG:
        return Jimple.newNegExpr(source);
      case NOT_LONG:
        return getNotLongExpr(source);
      case NOT_INT:
        return getNotIntExpr(source);
      default:
        throw new RuntimeException("Invalid Opcode: " + opcode);
    }
  }

  /**
   * returns bitwise negation of an integer
   *
   * @param source
   * @return
   */
  private Value getNotIntExpr(Local source) {
    return Jimple.newXorExpr(source, IntConstant.getInstance(-1));
  }

  /**
   * returns bitwise negation of a long
   *
   * @param source
   * @return
   */
  private Value getNotLongExpr(Local source) {
    return Jimple.newXorExpr(source, LongConstant.getInstance(-1l));
  }
}
