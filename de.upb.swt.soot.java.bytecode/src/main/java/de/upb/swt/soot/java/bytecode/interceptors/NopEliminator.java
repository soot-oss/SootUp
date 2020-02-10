package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class NopEliminator implements BodyInterceptor {

  /**
   * Removes {@link JNopStmt}s from the passed body.
   *
   * @param originalBody The current body before transformation.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    List<Stmt> stmtList = originalBody.getStmts();
    // TODO: [ms] check whether its possible to collect the original collection if nothing changed
    List<Stmt> newStmtList =
        stmtList.stream()
            .parallel()
            .filter(stmt -> !(stmt instanceof JNopStmt))
            .collect(Collectors.toList());

    Stmt lastStmt = stmtList.get(stmtList.size() - 1);
    // keep (-> add) the last Stmt if it is (already filtered) JNopStmt and the target of a trap
    if (lastStmt instanceof JNopStmt) {
      boolean keepLastStmt = false;
      for (Trap trap : originalBody.getTraps()) {
        if (trap.getEndStmt() == lastStmt) {
          keepLastStmt = true;
          break;
        }
      }
      if (keepLastStmt) {
        newStmtList.add(lastStmt);
      }
    }

    // replace the Body if the lists of Stmts differ - here: the length is different
    return stmtList.size() != newStmtList.size()
        ? originalBody.withStmts(newStmtList)
        : originalBody;
  }
}
