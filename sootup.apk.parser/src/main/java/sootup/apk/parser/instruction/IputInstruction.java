package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class IputInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int source = i.getRegisterA();
    int object = i.getRegisterB();
    FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
    JInstanceFieldRef jInstanceFieldRef =
        Jimple.newInstanceFieldRef(
            body.getRegisterLocal(object), getSootFieldRef(f).getFieldSignature());
    Local sourceValue = body.getRegisterLocal(source);
    JAssignStmt jAssignStmt = getAssignStmt(sourceValue, jInstanceFieldRef);
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public IputInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
