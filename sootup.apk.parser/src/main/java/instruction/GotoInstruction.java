package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JGotoStmt;

public class GotoInstruction extends JumpInstruction implements DeferableInstruction {
    @Override
    public void jimplify(DexBody body) {
        if (getTargetInstruction(body).getStmt() != null) {
            body.add(gotoStatement());
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

    private JGotoStmt gotoStatement() {
        JGotoStmt go = Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(go);
        return go;
    }

    @Override
    public void deferredJimplify(DexBody body) {
        // TODO: The logic here should be insertAfter
        body.insertAfter(markerUnit, gotoStatement());
    }
}
