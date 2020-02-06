package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class NopEliminator implements BodyInterceptor {

  /**
   * Removes {@link JNopStmt}s from the passed body.
   * @param originalBody The current body before transformation.
   * @return The transformed body.
   */
  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    List<Stmt> stmtList = originalBody.getStmts();
    List<Stmt> copyList = new ArrayList<>(stmtList.size());
    copyList.addAll(stmtList);

    for(Stmt stmt : stmtList){
      if(stmt instanceof JNopStmt){
        boolean keepNop = false;
        if(stmtList.get(stmtList.size()-1) == stmt){
          for(Trap trap : originalBody.getTraps()){
            if(trap.getEndStmt() == stmt){
              keepNop = true;
            }
          }
        }
        if(!keepNop){
          copyList.remove(stmt);
        }
      }
    }

    return originalBody.withStmts(copyList);
  }
}
