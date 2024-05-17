package sootup.util;

import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.SimpleStmtPositionInfo;
import sootup.core.types.PrimitiveType.IntType;

public class StmtPosUtil {

  /**creates a dummy SimpleStmtPositionInfo
   * lineNumber 1
   *
   * @return a dummy SimpleStmtPositionInfo
   */
  public static SimpleStmtPositionInfo createDummySimpleStmtPositionInfo(){
    return new SimpleStmtPositionInfo(1);
  }

}
