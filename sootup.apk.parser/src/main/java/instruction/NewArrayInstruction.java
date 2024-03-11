package instruction;

import Util.DexUtil;
import java.util.HashSet;
import java.util.Set;
import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction22c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ArrayType;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class NewArrayInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    if (!(instruction instanceof Instruction22c)) {
      throw new IllegalArgumentException(
          "Expected Instruction22c but got: " + instruction.getClass());
    }

    Instruction22c newArray = (Instruction22c) instruction;
    int dest = newArray.getRegisterA();

    Local size = body.getRegisterLocal(newArray.getRegisterB());
    Type t = DexUtil.toSootType(((TypeReference) newArray.getReference()).getType(), 0);
    // NewArrayExpr needs the ElementType as it increases the array dimension by 1
    Type elementType = ((ArrayType) t).getElementType();

    JNewArrayExpr jNewArrayExpr = JavaJimple.getInstance().newNewArrayExpr(elementType, size);

    Local l = body.getRegisterLocal(dest);
    JAssignStmt assign =
        Jimple.newAssignStmt(l, jNewArrayExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(assign);
    body.add(assign);
  }

  public NewArrayInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  boolean overridesRegister(int register) {
    TwoRegisterInstruction i = (TwoRegisterInstruction) instruction;
    int dest = i.getRegisterA();
    return register == dest;
  }

  @Override
  public Set<Type> introducedTypes() {
    ReferenceInstruction i = (ReferenceInstruction) instruction;

    Set<Type> types = new HashSet<Type>();
    types.add(DexUtil.toSootType(((TypeReference) i.getReference()).getType(), 0));
    return types;
  }
}
