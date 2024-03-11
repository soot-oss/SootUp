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
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JInstanceOfExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.Type;

public class InstanceOfInstruction extends DexLibAbstractInstruction {
  @Override
  public void jimplify(DexBody body) {
    Instruction22c i = (Instruction22c) instruction;
    int dest = i.getRegisterA();
    int source = i.getRegisterB();
    Type sootType = DexUtil.toSootType(((TypeReference) i.getReference()).getType(), 0);

    JInstanceOfExpr jInstanceOfExpr =
        Jimple.newInstanceOfExpr(body.getRegisterLocal(source), sootType);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jInstanceOfExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }

  public InstanceOfInstruction(Instruction instruction, int codeAddress) {
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

    Set<Type> types = new HashSet<>();
    types.add(DexUtil.toSootType(((TypeReference) i.getReference()).getType(), 0));
    return types;
  }
}
