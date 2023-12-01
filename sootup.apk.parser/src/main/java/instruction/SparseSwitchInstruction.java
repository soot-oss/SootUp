package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SparseSwitchInstruction extends SwitchInstruction {

    public SparseSwitchInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
        JSwitchStmt switchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(switchStmt);
        // It is unlike the PackedSwitchInstruction, here only one branching statement should be. (I literally don't know why :( )
//        targets.add(defaultTarget);
//        body.addBranchingStmt(switchStmt, targets);
        return switchStmt;
    }

    @Override
    public void computeDataOffsets(DexBody body) {

    }
}
