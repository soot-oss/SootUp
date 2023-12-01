package instruction;

import Util.DexUtil;
import java.util.HashSet;
import java.util.Set;
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
import sootup.core.types.Type;
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
            body.getRegisterLocal(dest),
            classConstant,
            StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public ConstClassInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  boolean overridesRegister(int register) {
    OneRegisterInstruction i = (OneRegisterInstruction) instruction;
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
