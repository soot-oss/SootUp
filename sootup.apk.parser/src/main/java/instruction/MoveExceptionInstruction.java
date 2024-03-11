package instruction;

import java.util.List;
import java.util.Set;
import main.DexBody;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.Type;
import sootup.java.core.language.JavaJimple;

public class MoveExceptionInstruction extends DexLibAbstractInstruction
    implements ReTypeableInstruction {

  protected Type realType;
  protected JIdentityStmt stmtToRetype;

  @Override
  public void jimplify(DexBody body) {
    int dest = ((OneRegisterInstruction) instruction).getRegisterA();
    Local l = body.getRegisterLocal(dest);
    JCaughtExceptionRef ref = JavaJimple.getInstance().newCaughtExceptionRef();
    stmtToRetype = Jimple.newIdentityStmt(l, ref, StmtPositionInfo.getNoStmtPositionInfo());
    setStmt(stmtToRetype);
    body.add(stmtToRetype);
  }

  public MoveExceptionInstruction(Instruction instruction, int codeAddress) {
    super(instruction, codeAddress);
  }

  @Override
  public void setRealType(DexBody body, Type t) {
    realType = t;
    body.addRetype(this);
  }

  @Override
  public void retype(List<Stmt> stmtList, Set<Local> locals) {
    if (realType == null) {
      throw new RuntimeException(
          "Real type of this instruction has not been set or was already retyped: " + this);
    }
    if (stmtList.contains(stmtToRetype)) {
      Local l = stmtToRetype.getLeftOp();
      l = l.withType(realType);
      locals.remove(l);
      locals.add(l);
      realType = null;
    }
  }
}
