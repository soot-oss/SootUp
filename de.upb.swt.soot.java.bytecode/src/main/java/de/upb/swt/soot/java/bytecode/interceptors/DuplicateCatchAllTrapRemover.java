package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Some compilers generate duplicate traps:
 *
 * <p>Exception table: from to target type 9 30 37 Class java/lang/Throwable 9 30 44 any 37 46 44
 * any
 *
 * <p>The semantics is as follows:
 *
 * <p>try { // block } catch { // handler 1 } finally { // handler 2 }
 *
 * <p>In this case, the first trap covers the block and jumps to handler 1. The second trap also
 * covers the block and jumps to handler 2. The third trap covers handler 1 and jumps to handler 2.
 * If we treat "any" as java.lang. Throwable, the second handler is clearly unnecessary. Worse, it
 * violates Soot's invariant that there may only be one handler per combination of covered code
 * region and jump target.
 *
 * <p>This transformer detects and removes such unnecessary traps.
 *
 * @author Steven Arzt
 * @author Marcus Nachtigall
 */
public class DuplicateCatchAllTrapRemover implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // Find two traps that use java.lang.Throwable as their type and that span the same code region
    Collection<Trap> originalTraps = originalBody.getTraps();
    List<Trap> traps = new ArrayList<>(originalBody.getTraps());
    for (Trap trap1 : originalTraps) {
      if (trap1.getExceptionType().getClassName().equals("java.lang.Throwable")) {
        for (Trap trap2 : originalTraps) {
          if (trap1 != trap2
              && trap1.getBeginStmt() == trap2.getBeginStmt()
              && trap1.getEndStmt() == trap2.getEndStmt()
              && trap2.getExceptionType().getClassName().equals("java.lang.Throwable")) {
            // Both traps (t1, t2) span the same code and catch java.lang.Throwable.
            // Check if one trap jumps to a target that then jumps to the target of the other trap
            for (Trap trap3 : traps) {
              if (trap3 != trap1
                  && trap3 != trap2
                  && trap3.getExceptionType().getClassName().equals("java.lang.Throwable")) {
                if (trapCoversStmt(originalBody, trap3, trap1.getHandlerStmt())
                    && trap3.getHandlerStmt() == trap2.getHandlerStmt()) {
                  // c -> t1 -> t3 -> t2 && x -> t2
                  traps.remove(trap2);
                  break;
                } else if (trapCoversStmt(originalBody, trap3, trap2.getHandlerStmt())
                    && trap3.getHandlerStmt() == trap1.getHandlerStmt()) {
                  // c -> t2 -> t3 -> t1 && c -> t1
                  traps.remove(trap1);
                  break;
                }
              }
            }
          }
        }
      }
    }
    return originalBody.withTraps(traps);
  }

  /**
   * Checks whether the given trap covers the given unit, i.e., there is an exceptional control flow
   * from the given unit to the given trap
   *
   * @param body The body containing the stmt and the trap
   * @param trap The trap
   * @param stmt The unit
   * @return True if there can be an exceptional control flow from the given unit to the given trap
   */
  private boolean trapCoversStmt(Body body, Trap trap, Stmt stmt) {
    List<Stmt> bodyStmts = body.getStmts();
    List<Stmt> sequence =
        bodyStmts.subList(
            bodyStmts.indexOf(trap.getBeginStmt()), bodyStmts.indexOf(trap.getEndStmt()));
    for (Stmt st : sequence) {
      if (st == stmt) {
        return true;
      }
    }
    return false;
  }
}
