package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.SwitchElement;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;

import java.util.ArrayList;
import java.util.List;

public class PackedSwitchInstruction extends SwitchInstruction {
    @Override
    public void jimplify(DexBody body) {

    }

    public PackedSwitchInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    protected Stmt switchStatement(DexBody body, Instruction targetData, Local key) {
        PackedSwitchPayload i = (PackedSwitchPayload) targetData;
        List<? extends SwitchElement> seList = i.getSwitchElements();

        // the default target always follows the switch statement
        int defaultTargetAddress = codeAddress + instruction.getCodeUnits();
        Stmt defaultTarget = body.instructionAtAddress(defaultTargetAddress).getStmt();

        List<IntConstant> lookupValues = new ArrayList<IntConstant>();
        List<Stmt> targets = new ArrayList<Stmt>();
        for (SwitchElement se : seList) {
            lookupValues.add(IntConstant.getInstance(se.getKey()));
            int offset = se.getOffset();
            targets.add(body.instructionAtAddress(codeAddress + offset).stmt);
        }
        JSwitchStmt jSwitchStmt = Jimple.newLookupSwitchStmt(key, lookupValues, StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(jSwitchStmt);
        return jSwitchStmt;
    }

    @Override
    public void computeDataOffsets(DexBody body) {

    }
}
