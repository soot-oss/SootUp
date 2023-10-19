package instruction;

import Util.DexUtil;
import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.Type;

public class IputInstruction extends FieldInstruction {
    @Override
    public void jimplify(DexBody body) {
        TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
        int source = i.getRegisterA();
        int object = i.getRegisterB();
        FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
        JInstanceFieldRef jInstanceFieldRef = Jimple.newInstanceFieldRef(body.getRegisterLocal(object), getSootFieldRef(f).getFieldSignature());
        Local sourceValue = body.getRegisterLocal(source);
        JAssignStmt jAssignStmt = getAssignStmt(body, sourceValue, jInstanceFieldRef);
        setStmt(jAssignStmt);
        body.add(jAssignStmt);
    }

    public IputInstruction(Instruction instruction, int codeAddress) {
        super(instruction, codeAddress);
    }
    @Override
    protected Type getTargetType(DexBody body) {
        FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
        return DexUtil.toSootType(f.getType(), 0);
    }
}
