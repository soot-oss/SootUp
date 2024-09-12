package sootup.apk.parser.instruction;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.TypeReference;
import sootup.apk.parser.Util.DexUtil;
import sootup.apk.parser.main.DexBody;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.types.ClassType;

public class NewInstanceInstruction extends DexLibAbstractInstruction {
  public NewInstanceInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void jimplify(DexBody body) {
    Instruction21c i = (Instruction21c) instruction;
    int dest = i.getRegisterA();
    String className = DexUtil.dottedClassName(((TypeReference) (i.getReference())).toString());
    ClassType classType = DexUtil.getClassTypeFromClassName(className);
    JNewExpr jNewExpr = Jimple.newNewExpr(classType);
    JAssignStmt jAssignStmt =
        Jimple.newAssignStmt(
            body.getRegisterLocal(dest), jNewExpr, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(jAssignStmt);
    body.add(jAssignStmt);
  }
}
