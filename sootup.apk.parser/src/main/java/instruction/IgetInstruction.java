package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class IgetInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    int object = i.getRegisterB();
    FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
    JInstanceFieldRef jInstanceFieldRef =
        Jimple.newInstanceFieldRef(
            body.getRegisterLocal(object), getSootFieldRef(f).getFieldSignature());
    JAssignStmt assignStmt = getAssignStmt(body, body.getRegisterLocal(dest), jInstanceFieldRef);
    setStmt(assignStmt);
    body.add(assignStmt);
  }

  public IgetInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

}
