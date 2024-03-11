package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.TypeReference;
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

public class FilledNewArrayRangeInstruction extends FilledArrayInstruction {
    @Override
    public void jimplify(DexBody body) {
        if (!(instruction instanceof Instruction3rc)) {
//            throw new IllegalArgumentException("Expected Instruction3rc but got: " + instruction.getClass());
            return;
        }

        Instruction3rc filledNewArrayInstr = (Instruction3rc) instruction;

        int usedRegister = filledNewArrayInstr.getRegisterCount();
        Type t = Util.DexUtil.toSootType(((TypeReference) filledNewArrayInstr.getReference()).getType(), 0);
        // NewArrayExpr needs the ElementType as it increases the array dimension by 1
        Type arrayType = ((ArrayType) t).getElementType();
        JNewArrayExpr arrayExpr = JavaJimple.getInstance().newNewArrayExpr(arrayType, IntConstant.getInstance(usedRegister));
        Local arrayLocal = body.getStoreResultLocal();
        JAssignStmt assignStmt = Jimple.newAssignStmt(arrayLocal, arrayExpr, StmtPositionInfo.getNoStmtPositionInfo());
        body.add(assignStmt);

        for (int i = 0; i < usedRegister; i++) {
            JArrayRef arrayRef = JavaJimple.getInstance().newArrayRef(arrayLocal, IntConstant.getInstance(i));

            JAssignStmt assign
                    = Jimple.newAssignStmt(arrayRef, body.getRegisterLocal(i + filledNewArrayInstr.getStartRegister()), StmtPositionInfo.getNoStmtPositionInfo());
            body.add(assign);
        }
        setStmt(assignStmt);
    }

    public FilledNewArrayRangeInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    public void finalize(DexBody body, DexLibAbstractInstruction successor) {

    }
}
