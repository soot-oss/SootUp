package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * A BodyInterceptor that attempts to identify and separate uses of a local variable that are
 * independent of each other.
 *
 * <p>For example the code:
 *
 * <pre>
 *     i0 = 0
 *     i0 = 1
 *     i1 = i0 + 1
 *     i0 = 5
 * </pre>
 *
 * <pre>
 *    i0#1 = 0
 *    i0#2 = 1
 *    i1 = i0#2 + 1
 *    i0#3 = 5
 * </pre>
 *
 * @author Zun Wang
 */
public class LocalSplitter implements BodyInterceptor {

  public Body interceptBody(@Nonnull Body originalBody) {


    return originalBody;
  }

  // ******************assist_functions*************************

  /**
   * Use newDef to replace the def in oldStmt.
   *
   * @param oldStmt a Stmt whose def is to be replaced.
   * @param newDef a Local is to replace def of oldStmt
   * @return a new Stmt with newDef
   */
  @Nonnull
  protected Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if (oldStmt instanceof JAssignStmt) {
      return Jimple.newAssignStmt(
          newDef, ((JAssignStmt) oldStmt).getRightOp(), oldStmt.getPositionInfo());
    } else {
      throw new RuntimeException("Just JAssignStmt allowed");
    }
  }

  /**
   * Use newUse to replace the oldUse in oldStmt
   *
   * @param oldStmt a Stmt that has oldUse
   * @param oldUse a Local in the useList of oldStmt
   * @param newUse a Local is to replace oldUse
   * @return a new Stmt with newUse
   */
  @Nonnull
  protected Stmt withNewUse(@Nonnull Stmt oldStmt, @Nonnull Local oldUse, @Nonnull Local newUse) {
    ReplaceUseStmtVisitor visitor = new ReplaceUseStmtVisitor(oldUse, newUse);
    oldStmt.accept(visitor);
    return visitor.getNewStmt();
  }

  /**
   * Check whether a Stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  @Nonnull
  protected boolean hasModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    boolean isModified = false;
    if (!stmt.getUses().isEmpty()) {
      for (Value use : stmt.getUses()) {
        if (use instanceof Local) {
          String name = ((Local) use).getName();
          if (name.contains("#")) {
            int i = name.indexOf('#');
            if (name.substring(0, i).equals(oriLocal.getName())) {
              isModified = true;
              break;
            }
          }
        }
      }
    }
    return isModified;
  }

  /**
   * Check whether stmt's useList contains the given modified oriLocal
   *
   * @param stmt: a stmt
   * @param oriLocal: a local
   * @return if so, return this modified local, else return null
   */
  protected Local getModifiedUse(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    Local modifiedLocal = null;
    if (hasModifiedUse(stmt, oriLocal)) {
      if (!stmt.getUses().isEmpty()) {
        for (Value use : stmt.getUses()) {
          if (use instanceof Local) {
            String name = ((Local) use).getName();
            if (name.contains("#")) {
              int i = name.indexOf('#');
              if (name.substring(0, i).equals(oriLocal.getName())) {
                modifiedLocal = (Local) use;
                break;
              }
            }
          }
        }
      }
    }
    return modifiedLocal;
  }

  /**
   * Check whether a Stmt's def is the modified oriLocal
   *
   * @param stmt: a stmt is to be checked
   * @param oriLocal: a local is to be checked
   * @return if so, return true, else return false
   */
  @Nonnull
  protected boolean hasModifiedDef(@Nonnull Stmt stmt, @Nonnull Local oriLocal) {
    boolean isModified = false;
    if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
      String name = ((Local) stmt.getDefs().get(0)).getName();
      if (name.contains("#")) {
        int i = name.indexOf('#');
        if (name.substring(0, i).equals(oriLocal.getName())) {
          isModified = true;
        }
      }
    }
    return isModified;
  }

  /**
   * Check whether leftLocal's name has bigger index than rightLocal's.
   *
   * @param leftLocal: a local in form oriLocal#num1
   * @param rigthLocal: a local in form oriLocal#num2
   * @return if so return true, else return false
   */
  @Nonnull
  protected boolean isBiggerName(@Nonnull Local leftLocal, @Nonnull Local rigthLocal) {
    boolean isBigger = false;
    String leftName = leftLocal.getName();
    String rightName = rigthLocal.getName();
    int i = leftName.indexOf('#');
    int j = rightName.indexOf('#');
    int leftNum = Integer.parseInt(leftName.substring(i + 1));
    int rightNum = Integer.parseInt(rightName.substring(j + 1));
    if (leftNum > rightNum) {
      isBigger = true;
    }
    return isBigger;
  }
}
