package instruction;

import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.core.types.UnknownType;
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
    JAssignStmt assign = getAssignStmt(body, sourceValue, jArrayRef);
    setStmt(assign);
    body.add(assign);
  }

  public AputInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  protected Type getTargetType(DexBody body) {
    Instruction23x aPutInstr = (Instruction23x) instruction;
    Type t = body.getRegisterLocal(aPutInstr.getRegisterB()).getType();
    if (t instanceof ArrayType) {
      return ((ArrayType) t).getElementType();
    } else {
      return UnknownType.getInstance();
    }
  }
}
