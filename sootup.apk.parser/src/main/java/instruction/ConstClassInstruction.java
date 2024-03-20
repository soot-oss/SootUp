package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.java.core.language.JavaJimple;

public class ConstClassInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {

    if (!(instruction instanceof Instruction21c)) {
      throw new IllegalArgumentException(
          "Expected Instruction21c but got: " + instruction.getClass());
    }

    ReferenceInstruction constClass = (ReferenceInstruction) this.instruction;

    TypeReference tidi = (TypeReference) (constClass.getReference());
    ClassConstant classConstant = JavaJimple.getInstance().newClassConstant(tidi.getType());
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();

    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), classConstant, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public ConstClassInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

}
