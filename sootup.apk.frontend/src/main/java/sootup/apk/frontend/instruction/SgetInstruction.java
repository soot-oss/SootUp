package sootup.apk.frontend.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import sootup.apk.frontend.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;

public class SgetInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    FieldReference f = (FieldReference) ((ReferenceInstruction) instruction).getReference();
    JStaticFieldRef r = Jimple.newStaticFieldRef(getStaticSootFieldRef(f).getFieldSignature());
    JAssignStmt assign =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), r, StmtPositionInfo.getNoStmtPositionInfo());
    body.add(assign);
    setStmt(assign);
  }

  public SgetInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
