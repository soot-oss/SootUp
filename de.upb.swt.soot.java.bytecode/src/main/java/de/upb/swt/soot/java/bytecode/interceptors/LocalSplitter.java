package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
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
    Hashtable<Local, Boolean> defLocalTable = new Hashtable<>();

    // Copy all original Stmts into a newStmts List
    List<Stmt> newStmts = new ArrayList<>();
    List<Stmt> stmts = originalBody.getStmts();

    for (Stmt stmt : stmts) {
      // Fixme: is stmt really copied??????
      Stmt newStmt = stmt;
      newStmts.add(newStmt);
      if (!stmt.getDefs().isEmpty()) {
        Value def = stmt.getDefs().get(0);
        if (def instanceof Local) {
          if (!defLocalTable.containsKey(def)) {
            defLocalTable.put((Local) def, false);
          }
        }
      }
    }
    // Copy all original Locals into a newLocals set
    Set<Local> newLocals = new HashSet<Local>();
    Set<Local> locals = originalBody.getLocals();
    for (Local local : locals) {
      // Fixme: the same question
      Local newLocal = local;
      newLocals.add(newLocal);
    }

    int newLocalIndex = 0;
    for (Stmt stmt : stmts) {
      if (!stmt.getDefs().isEmpty() && stmt.getDefs().get(0) instanceof Local) {
        Local oriLocal = (Local) stmt.getDefs().get(0);
        // If "oriLocal" as a def is visited, then it maybe need to split
        if (defLocalTable.get(oriLocal)) {
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
              if (!stmts.get(position).getDefs().isEmpty()
                  && stmts.get(position).getDefs().contains(oriLocal)) {
                break;
              }
            }
            if (!stmts.get(position).getUses().isEmpty()
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
          defLocalTable.replace(oriLocal, true);
        }
      }
    }

    Body newBody = originalBody.withLocals(newLocals);
    newBody = newBody.withStmts(newStmts);
    return newBody;
  }

  protected Stmt withNewDef(Stmt oldStmt, Local newDef) {
    // TODO: Implements
    return null;
  }

  protected Stmt withNewUse(Stmt oldStmt, Local oldUse, Local newUse) {
    // TODO: Implement
    return null;
  }
}
