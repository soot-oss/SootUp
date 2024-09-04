package sootup.apk.parser.instruction;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.NarrowLiteralInstruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class ConstInstruction extends DexLibAbstractInstruction {

  public ConstInstruction(Instruction instruction, int codeAdress) {
    super(instruction, codeAdress);
  }

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    Constant cst = getConstant();
    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), cst, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  /** Return the literal constant for this instruction. */
  private Constant getConstant() {
    long literal = 0;
    if (instruction instanceof WideLiteralInstruction) {
      literal = ((WideLiteralInstruction) instruction).getWideLiteral();
    } else if (instruction instanceof NarrowLiteralInstruction) {
      literal = ((NarrowLiteralInstruction) instruction).getNarrowLiteral();
    } else {
      throw new RuntimeException("literal error: expected narrow or wide literal.");
    }

    Opcode opcode = instruction.getOpcode();
    switch (opcode) {
      case CONST:
      case CONST_4:
      case CONST_HIGH16:
      case CONST_16:
        return IntConstant.getInstance((int) literal);

      case CONST_WIDE_HIGH16:
      case CONST_WIDE:
      case CONST_WIDE_16:
      case CONST_WIDE_32:
        return LongConstant.getInstance(literal);
      default:
        throw new IllegalArgumentException(
            "Expected a const or a const-wide instruction, got neither.");
    }
  }
}
