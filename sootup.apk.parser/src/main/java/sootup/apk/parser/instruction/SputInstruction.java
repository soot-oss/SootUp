package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class SputInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    int source = ((OneRegisterInstruction) instruction).getRegisterA();
    FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
    JStaticFieldRef instanceField =
        Jimple.newStaticFieldRef(getStaticSootFieldRef(f).getFieldSignature());
    Local sourceValue = body.getRegisterLocal(source);
    JAssignStmt assign = getAssignStmt(sourceValue, instanceField);
    setStmt(assign);
    body.add(assign);
  }

  public SputInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
