package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;

import java.util.Collections;

public class GotoInstruction extends JumpInstruction implements DeferableInstruction {
    @Override
    public void jimplify(DexBody body) {
        if (getTargetInstruction(body).getStmt() != null /*&& !(targetInstruction.stmt instanceof JNopStmt)*/ ) {
            JGotoStmt jGotoStmt = gotoStatement(body);
            body.add(jGotoStmt);
//            body.addBranchingStmt(jGotoStmt, Collections.singletonList(targetInstruction.stmt));
            return;
        }
        body.addDeferredJimplification(this);
        markerUnit = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
        stmt = markerUnit;
        body.add(markerUnit);
    }

    public GotoInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    private JGotoStmt gotoStatement(DexBody body) {
        JGotoStmt go = Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
        body.addBranchingStmt(go, Collections.singletonList(targetInstruction.stmt));
        setStmt(go);
        return go;
    }

    @Override
    public void deferredJimplify(DexBody body) {
        JGotoStmt jGotoStmt = gotoStatement(body);
//        Stmt labelStmt;
//        if(targetInstruction.stmt == null){
//            labelStmt = Util.Util.makeStmt(targetInstruction);
//        }
//        else{
//            labelStmt = targetInstruction.stmt;
//        }
//        if(!(targetInstruction.stmt instanceof JGotoStmt)) {
//            body.addBranchingStmt(jGotoStmt, Collections.singletonList(labelStmt));
//        }
        body.insertAfter(jGotoStmt, markerUnit);
    }
}
