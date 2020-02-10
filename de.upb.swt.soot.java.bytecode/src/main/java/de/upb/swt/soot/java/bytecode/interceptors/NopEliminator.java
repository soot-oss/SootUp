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
    final Stmt lastStmt = stmtList.get(stmtList.size() - 1);
    final boolean isLastStmtJNop = lastStmt instanceof JNopStmt;
    boolean keepLastStmt = !isLastStmtJNop;

    if (isLastStmtJNop) {
      for (Trap trap : originalBody.getTraps()) {
        if (trap.getEndStmt() == lastStmt) {
          keepLastStmt = true;
          break;
        }
      }
    }

    // [ms] possible performance hint? iterate && filter only once; remember index positions of
    // relevant sequences; add them "by hand"
    long size = stmtList.parallelStream().filter(stmt -> !(stmt instanceof JNopStmt)).count();
    if (keepLastStmt) {
      size++;
    }

    List<Stmt> newStmtList;
    if (stmtList.size() == size) {
      return originalBody;
    }

    long finalSize = size;
    newStmtList =
        stmtList
            .parallelStream()
            .filter(stmt -> !(stmt instanceof JNopStmt))
            .collect(Collectors.toCollection(() -> new ArrayList<Stmt>((int) finalSize)));
    // keep (-> add) the last Stmt if it is a JNopStmt (-> filtered) but used in a trap
    if (keepLastStmt) {
      newStmtList.add(lastStmt);
    }
    return originalBody.withStmts(newStmtList);
  }
}
