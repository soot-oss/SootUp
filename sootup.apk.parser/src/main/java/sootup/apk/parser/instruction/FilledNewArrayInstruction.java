package sootup.apk.parser.instruction;

import org.jf.dexlib2.dexbacked.instruction.DexBackedInstruction35c;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.parser.Util.DexUtil;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class FilledNewArrayInstruction extends FilledArrayInstruction {
  @Override
  public void jimplify(DexBody body) {
    DexBackedInstruction35c filledNewArrayInstr = (DexBackedInstruction35c) instruction;
    int[] regs = {
      filledNewArrayInstr.getRegisterC(),
      filledNewArrayInstr.getRegisterD(),
      filledNewArrayInstr.getRegisterE(),
      filledNewArrayInstr.getRegisterF(),
      filledNewArrayInstr.getRegisterG(),
    };
    int usedRegister = filledNewArrayInstr.getRegisterCount();

    Type t = DexUtil.toSootType(((TypeReference) filledNewArrayInstr.getReference()).getType(), 0);
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type arrayType = ((ArrayType) t).getElementType();
    JNewArrayExpr arrayExpr =
        JavaJimple.getInstance().newNewArrayExpr(arrayType, IntConstant.getInstance(usedRegister));
    // new local generated intentional, will be moved to real register by MoveResult
    Local arrayLocal = body.getStoreResultLocal();
    JAssignStmt assign =
        Jimple.newAssignStmt(arrayLocal, arrayExpr, StmtPositionInfo.getNoStmtPositionInfo());
    body.add(assign);
    for (int i = 0; i < usedRegister; i++) {
      JArrayRef arrayRef =
          JavaJimple.getInstance().newArrayRef(arrayLocal, IntConstant.getInstance(i));
      JAssignStmt assign2 =
          Jimple.newAssignStmt(
              arrayRef, body.getRegisterLocal(regs[i]), StmtPositionInfo.getNoStmtPositionInfo());
      body.add(assign2);
    }
    setStmt(assign);
  }

  public FilledNewArrayInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void finalize(DexBody body, DexLibAbstractInstruction successor) {}
}
