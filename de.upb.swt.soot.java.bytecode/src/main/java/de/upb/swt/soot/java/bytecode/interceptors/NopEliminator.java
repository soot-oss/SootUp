package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/** @author Marcus Nachtigall, Markus Schmidt */
public class NopEliminator implements BodyInterceptor {

  /**
   * Removes {@link JNopStmt}s from the List of Stmts of the given {@link Body}. Complexity is
   * linear with respect to the statements.
   *
   * @param originalBody The current body before interception.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    List<Stmt> stmtList = originalBody.getStmts();
    if (stmtList.isEmpty()) {
      return originalBody;
    }
    final Stmt lastStmt = stmtList.get(stmtList.size() - 1);
    final boolean isLastStmtJNop = lastStmt instanceof JNopStmt;
    boolean copyLastStmt = false;

    if (isLastStmtJNop) {
      for (Trap trap : originalBody.getTraps()) {
        if (trap.getEndStmt() == lastStmt) {
          copyLastStmt = true;
          break;
        }
      }
    }

    // [ms] possible performance hint: iterate && filter only once; remember index positions of
    // relevant sequences; add them "by hand" -> last stmt could be excluded from loop, too
    final int newSize =
        (int) stmtList.parallelStream().filter(stmt -> !(stmt instanceof JNopStmt)).count()
            + (copyLastStmt ? 1 : 0);

    // nothing changed due to this interceptor
    if (stmtList.size() == newSize) {
      return originalBody;
    }

    List<Stmt> newStmtList =
        stmtList
            .parallelStream()
            .filter(stmt -> !(stmt instanceof JNopStmt))
            .collect(Collectors.toCollection(() -> new ArrayList<>(newSize)));

    // keep (-> copy) the last Stmt if it is a JNopStmt (-> already filtered) but used in a trap
    if (copyLastStmt) {
      newStmtList.add(lastStmt);
    }
    return originalBody.withStmts(newStmtList);
  }
}
