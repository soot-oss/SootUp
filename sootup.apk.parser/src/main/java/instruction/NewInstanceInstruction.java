package instruction;

import static Util.Util.*;
import static Util.Util.dottedClassName;

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
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class NewInstanceInstruction extends DexLibAbstractInstruction {
  public NewInstanceInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    Instruction21c i = (Instruction21c) instruction;
    int dest = i.getRegisterA();
    String className = dottedClassName(((TypeReference) (i.getReference())).toString());
    ClassType classType = getClassTypeFromClassName(className);
    JNewExpr jNewExpr = Jimple.newNewExpr(classType);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jNewExpr, StmtPositionInfo.createNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
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
