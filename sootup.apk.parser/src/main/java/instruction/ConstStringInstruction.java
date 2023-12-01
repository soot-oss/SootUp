package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.instruction.formats.Instruction31c;
import org.jf.dexlib2.iface.reference.StringReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.java.core.JavaIdentifierFactory;

public class ConstStringInstruction extends DexLibAbstractInstruction {
  public ConstStringInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    String s;
    if (instruction instanceof Instruction21c) {
      Instruction21c i = (Instruction21c) instruction;
      s = ((StringReference) (i.getReference())).getString();
    } else if (instruction instanceof Instruction31c) {
      Instruction31c i = (Instruction31c) instruction;
      s = ((StringReference) (i.getReference())).getString();
    } else {
      throw new IllegalArgumentException(
          "Expected Instruction21c or Instruction31c but got neither.");
    }
    StringConstant stringConstant =
        new StringConstant(s, JavaIdentifierFactory.getInstance().getType("java.lang.String"));
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest),
            stringConstant,
            StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }
}
