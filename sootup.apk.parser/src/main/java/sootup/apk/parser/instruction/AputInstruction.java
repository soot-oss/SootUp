package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.java.core.language.JavaJimple;

public class AputInstruction extends FieldInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction23x)) {
      throw new IllegalArgumentException(
          "Expected Instruction23x but got: " + instruction.getClass());
    }

    Instruction23x aPutInstr = (Instruction23x) instruction;
    int source = aPutInstr.getRegisterA();

    Local arrayBase = body.getRegisterLocal(aPutInstr.getRegisterB());
    Local index = body.getRegisterLocal(aPutInstr.getRegisterC());
    JArrayRef jArrayRef = JavaJimple.getInstance().newArrayRef(arrayBase, index);

    Local sourceValue = body.getRegisterLocal(source);
    JAssignStmt assign = getAssignStmt(sourceValue, jArrayRef);
    setStmt(assign);
    body.add(assign);
  }

  public AputInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }
}
