package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Steven Arzt,  Marcus Nachtigall and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
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
 * <p>This interceptor detects and removes such unnecessary traps.
 *
 * @author Steven Arzt
 * @author Marcus Nachtigall
 */
public class DuplicateCatchAllTrapRemover implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {

    // TODO: [ms] this algorithms costs are (currently) cubic!
    // maybe sorting it into some kind of interval tree and retrieve overlaps will help to reduce
    // costs

    final List<Trap> traps = builder.getTraps();
    if (traps.size() < 3) {
      return;
    }
    final List<Stmt> stmtList = builder.getStmts();

    // Find two traps that use java.lang.Throwable as their type and that span the same code region
    for (int i = 0, trapsSize = traps.size(); i < trapsSize; i++) {
      Trap trap1 = traps.get(i);
      // FIXME(#430): [ms] adapt to work with java module, too
      if (trap1.getExceptionType().getFullyQualifiedName().equals("java.lang.Throwable")) {
        for (int j = 0; j < trapsSize; j++) {
          Trap trap2 = traps.get(j);
          if (trap1 != trap2
              && trap1.getBeginStmt() == trap2.getBeginStmt()
              && trap1.getEndStmt() == trap2.getEndStmt()
              && trap2.getExceptionType().getFullyQualifiedName().equals("java.lang.Throwable")) {
            // Both traps (t1, t2) span the same code and catch java.lang.Throwable.
            // Check if one trap jumps to a target that then jumps to the target of the other trap
            for (int k = 0; k < trapsSize; k++) {

              Trap trap3 = traps.get(k);
              final int startIdx = stmtList.indexOf(trap3.getBeginStmt());
              final int endIdx = stmtList.indexOf(trap3.getEndStmt()); // endstmt is exclusive!

              if (trap3 != trap1
                  && trap3 != trap2
                  && trap3
                      .getExceptionType()
                      .getFullyQualifiedName()
                      .equals("java.lang.Throwable")) {
                if (trapCoversStmt(stmtList, startIdx, endIdx, trap1.getHandlerStmt())
                    && trap3.getHandlerStmt() == trap2.getHandlerStmt()) {
                  // c -> t1 -> t3 -> t2 && x -> t2
                  traps.remove(trap2);
                  j--;
                  trapsSize--;
                  break;
                } else if (trapCoversStmt(stmtList, startIdx, endIdx, trap2.getHandlerStmt())
                    && trap3.getHandlerStmt() == trap1.getHandlerStmt()) {
                  // c -> t2 -> t3 -> t1 && c -> t1
                  traps.remove(trap1);
                  i--;
                  trapsSize--;
                  break;
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Checks whether the given trap covers the given stmt, i.e., there is an exceptional control flow
   * from the given stmt to the given trap
   *
   * @param bodyStmts linearized Stmtgraph
   * @param stmt The unit
   * @return True if there can be an exceptional control flow from the given unit to the given trap
   */
  private boolean trapCoversStmt(
      @Nonnull List<Stmt> bodyStmts, int trapBegin, int trapEnd, @Nonnull Stmt stmt) {
    for (int i = trapBegin; i < trapEnd; i++) {
      if (bodyStmts.get(i) == stmt) {
        return true;
      }
    }
    return false;
  }
}
