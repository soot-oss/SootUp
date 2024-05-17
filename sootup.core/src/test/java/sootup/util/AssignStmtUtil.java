package sootup.util;

import java.util.Collections;
import sootup.core.jimple.basic.LValue;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JAssignStmtTest;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType.IntType;

public class AssignStmtUtil {



  private static Local createDummyLocal(){
    return new Local("a", IntType.getInstance());
  }

  /** will return a dummy assignment statement with an invoke expression
   * the left value will be the dummy int local
   * the right value will be the dummy static invoke expression
   * stmt position is the dummy SimpleStatementPositionInfo
   *
   * @return a dummy JAssignStmt with a static invoke expression
   */
  public static JAssignStmt createDummyAssignStmtWithExpr() {
    Local local = LocalUtil.createDummyLocalForInt();
    SimpleStmtPositionInfo pos = StmtPosUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(local,InvokeExprUtil.createDummyStaticInvokeExpr(),pos);
  }

  /** will return a dummy assignment statement
   * the left value will be the dummy static field ref
   * the right value will be the dummy local
   * stmt position is the dummy SimpleStatementPositionInfo
   *
   * @return a dummy JAssignStmt with a static field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmtWithStaticFieldRefLeft() {
    JFieldRef fieldRef = FieldRefUtil.createDummyStaticFieldRef();
    SimpleStmtPositionInfo pos = StmtPosUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(fieldRef,LocalUtil.createDummyLocalForInt(),pos);
  }

  /** will return a dummy assignment statement
   * stmt position is the dummy SimpleStatementPositionInfo
   *
   * @param left defines the left value of the dummy assign statement
   * @param right defines the right value of the dummy assign statement
   *
   * @return a dummy JAssignStmt with an instance field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmt(LValue left, Value right) {
    SimpleStmtPositionInfo pos = StmtPosUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(left,right,pos);
  }


  /** will return a dummy assignment statement
   * the right value will be the dummy local
   * the left value will be the dummy local
   * stmt position is the dummy SimpleStatementPositionInfo
   *
   * @return a dummy JAssignStmt with a static field ref on the left side
   */
  public static JAssignStmt createDummyAssignStmtWithLocals() {
    SimpleStmtPositionInfo pos = StmtPosUtil.createDummySimpleStmtPositionInfo();
    return new JAssignStmt(LocalUtil.createDummyLocalForInt(),LocalUtil.createDummyLocalForInt(),pos);
  }

}
