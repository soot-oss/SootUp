package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.model.Body;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class MoveExceptionInstruction extends DexLibAbstractInstruction implements ReTypeableInstruction {

    protected Type realType;
    protected JIdentityStmt stmtToRetype;
    @Override
    public void jimplify(DexBody body) {
        int dest = ((OneRegisterInstruction) instruction).getRegisterA();
        Local l = body.getRegisterLocal(dest);
        JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
        stmtToRetype = Jimple.newIdentityStmt(l, ref, StmtPositionInfo.createNoStmtPositionInfo());
        setStmt(stmtToRetype);
        body.add(stmtToRetype);
    }

    public MoveExceptionInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }

    @Override
    public void setRealType(DexBody body, Type t) {
        realType = t;
        body.addRetype(this);
    }

    @Override
    public void retype(Body body) {
        if (realType == null) {
            throw new RuntimeException("Real type of this instruction has not been set or was already retyped: " + this);
        }
        if (body.getStmts().contains(stmtToRetype)) {
            Local l = (Local) (stmtToRetype.getLeftOp());
            l = l.withType(realType);
            body.getLocals().remove(l);
            body.getLocals().add(l);
            realType = null;
        }
    }
}
