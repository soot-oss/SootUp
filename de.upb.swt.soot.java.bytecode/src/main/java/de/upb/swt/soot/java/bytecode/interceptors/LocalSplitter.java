package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
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
 * <p>for(int i; i < k; i++); for(int i; i < k; i++);
 *
 * <p>would be transformed into: for(int i; i < k; i++); for(int j; j < k; j++);
 */
public class LocalSplitter implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {

    // Collect all the Locals as def of a Stmt in a Hashtable:
    // key: collected Local
    // value: whether the Local has been visited, initialization is false
    HashMap<Local, Boolean> defLocalMap = new HashMap<>();

    List<Stmt> stmts = originalBody.getStmts();

    for (Stmt stmt : stmts) {
      if (!stmt.getDefs().isEmpty()) {
        Value def = stmt.getDefs().get(0);
        if (def instanceof Local) {
          if (!defLocalMap.containsKey(def)) {
            defLocalMap.put((Local) def, false);
          }
        }
      }
    }
    // Copy all original Locals into a newLocals set
    Set<Local> locals = originalBody.getLocals();
    Set<Local> newLocals = new HashSet<Local>();
    newLocals.addAll(locals);

    // Copy all original Stmt into a newStmts list
    List<Stmt> newStmts = new ArrayList<>();
    newStmts.addAll(stmts);


    int newLocalIndex = 0;
    for (Stmt stmt : stmts) {
      if ((!stmt.getDefs().isEmpty()) && stmt.getDefs().get(0) instanceof Local) {
        Local oriLocal = (Local) stmt.getDefs().get(0);
        // If "oriLocal" as a def is visited, then it maybe need to split
        if (defLocalMap.get(oriLocal)) {
          int startPos = stmts.indexOf(stmt);
          int position = startPos;
          boolean localBeUsed = false;
          Local newLocal = oriLocal.withName(oriLocal.getName() + "#" + newLocalIndex);

          // Check all Stmts from the startPos to the endPos, which Stmt uses the "oriLocal"
          // startPos: position of "stmt"
          // endPos: position of a Stmt whose def is "oriLocal" again.
          // If "oriLocal" has been used by these Stmt and "stmt" itself, then modify them
          while (position < stmts.size()) {
            if (position != startPos) {
              if ((!stmts.get(position).getDefs().isEmpty())
                  && stmts.get(position).getDefs().contains(oriLocal)) {
                break;
              }
            }
            if ((!stmts.get(position).getUses().isEmpty())
                && stmts.get(position).getUses().contains(oriLocal)) {
              localBeUsed = true;
              newStmts.set(position, withNewUse(newStmts.get(position), oriLocal, newLocal));
            }
            position++;
          }
          if (localBeUsed) {
            newStmts.set(startPos, withNewDef(stmt, newLocal));
            newLocals.add(newLocal);
            newLocalIndex++;
          }
        } else { // if this originalLocal is not visited, then change its value in table to true
          defLocalMap.replace(oriLocal, true);
        }
      }
    }

    Body newBody = originalBody.withLocals(newLocals);
    newBody = newBody.withStmts(newStmts);
    return newBody;
  }


  @Nonnull
  protected Stmt withNewDef(@Nonnull Stmt oldStmt, @Nonnull Local newDef) {
    if(oldStmt instanceof JAssignStmt){
      return Jimple.newAssignStmt(newDef, ((JAssignStmt) oldStmt).getRightOp(), oldStmt.getPositionInfo());
    }else{
      throw new RuntimeException("Just JAssignStmt allowed");
    }
  }

  protected Stmt withNewUse(Stmt oldStmt, Local oldUse, Local newUse) {
    // TODO: Implement
    return null;
  }
}
