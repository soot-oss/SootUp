package sootup.java.core.interceptors.Dex;

import java.util.List;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.java.core.language.JavaJimple;

public class DexTrapStackTransformer implements BodyInterceptor {
  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
    builder.getStmts().add(Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));
    List<Trap> traps = builder.getStmtGraph().getTraps();
    for (Trap trap : traps) {
      if (isCaughtExceptionRef(trap.getHandlerStmt())) {
        continue;
      }
      // Add the exception reference
      Local local = new LocalGenerator(builder.getLocals()).generateLocal(trap.getExceptionType());
      Stmt caughtStmt =
          Jimple.newIdentityStmt(
              local,
              JavaJimple.getInstance().newCaughtExceptionRef(),
              StmtPositionInfo.createNoStmtPositionInfo());
      builder.getStmts().add(caughtStmt);
      builder.getStmts().add(Jimple.newGotoStmt(StmtPositionInfo.createNoStmtPositionInfo()));
      replaceTrap(traps, trap, trap.withHandlerStmt(caughtStmt));
    }
  }

  public void replaceTrap(List<Trap> traps, Trap toBeReplaced, Trap newTrap) {
    int indexOf = traps.indexOf(toBeReplaced);
    if (indexOf != -1) {
      traps.set(indexOf, newTrap);
    }
  }

  /**
   * Checks whether the given statement stores an exception reference
   *
   * @param handlerUnit The statement to check
   * @return True if the given statement stores an exception reference, otherwise false
   */
  private boolean isCaughtExceptionRef(Stmt handlerUnit) {
    if (!(handlerUnit instanceof JIdentityStmt)) {
      return false;
    }
    JIdentityStmt stmt = (JIdentityStmt) handlerUnit;
    return stmt.getRightOp() instanceof JCaughtExceptionRef;
  }
}
