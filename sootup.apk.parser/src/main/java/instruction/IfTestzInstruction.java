package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21t;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.AbstractBinopExpr;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;

import java.util.Collections;

public class IfTestzInstruction extends ConditionalJumpInstruction {

    public IfTestzInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    protected JIfStmt ifStatement(DexBody dexBody) {
        Instruction21t i = (Instruction21t) instruction;
        AbstractConditionExpr condition = getComparisonExpr(dexBody, i.getRegisterA());
        JIfStmt jif = Jimple.newIfStmt(condition, StmtPositionInfo.createNoStmtPositionInfo());
//        Stmt labelStmt;
//        if(targetInstruction.stmt == null){
//            labelStmt = Util.Util.makeStmt(targetInstruction);
//        }
//        else{
//            labelStmt = targetInstruction.stmt;
//        }
        dexBody.addBranchingStmt(jif, Collections.singletonList(targetInstruction.stmt));
        return jif;
    }
}
