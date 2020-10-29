package de.upb.swt.soot.java.bytecode.interceptors;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.NullType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/jimple/toolkits/typing/TypeAssigner.java

/**
 * This transformer assigns types to local variables.
 *
 * @author Etienne Gagnon
 * @author Ben Bellamy
 * @author Eric Bodden
 */
public class TypeAssigner implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder builder) {
    // TODO Implement

  }

  // ************************************assist_functions*******************************

  /**
   * Remove all Stmts which use locals with {@link de.upb.swt.soot.core.types.NullType} and lead up
   * to NullPointerException at runtime.
   *
   * @param builder a instance of {@link de.upb.swt.soot.core.model.Body.BodyBuilder}
   */
  protected void removeNullPointerStmts(@Nonnull Body.BodyBuilder builder) {

    // Check whether there's a local with NullType. If no, return immediately
    for (Local local : builder.getLocals()) {
      if (local.getType() instanceof NullType) {
        return;
      }
    }

    // Iterate all Stmts to find out all Stmts leading up to NullPointerException
    List<Stmt> stmts = builder.getStmts();
    // Create a list to store the found Stmts
    List<Stmt> stmtsToRemove = new ArrayList<>();
    for (Stmt stmt : stmts) {
      List<Value> uses = stmt.getUses();
      for (Value use : uses) {
        if (use instanceof Local && use.getType() instanceof NullType) {
          // 1.case use is a base of ArrayRef
          if (stmt.containsArrayRef() && stmt.getArrayRef().getBase().equivTo(use)) {
            stmtsToRemove.add(stmt);
          }
          // 2.case use is an InstanceFieldRef
          else if (stmt.containsFieldRef()) {
            JFieldRef ref = stmt.getFieldRef();
            if (ref instanceof JInstanceFieldRef
                && ((JInstanceFieldRef) ref).getBase().equivTo(use)) {
              stmtsToRemove.add(stmt);
            }
          }
          // 3.case use is a base of InstanceInvokeExpr
          else if (stmt.containsInvokeExpr()) {
            AbstractInvokeExpr expr = stmt.getInvokeExpr();
            if (expr instanceof AbstractInstanceInvokeExpr
                && ((AbstractInstanceInvokeExpr) expr).getBase().equivTo((use))) {
              stmtsToRemove.add(stmt);
            }
          }
        }
      }
    }
    // Remove all collected Stmts from method's body
    for (Stmt stmt : stmtsToRemove) {
      builder = builder.removeStmt(stmt);
    }

    // After removing such Stmts, it's posssible, there could be dead assignments in body
    // If there were, eliminate them
    BodyInterceptor assignmentEliminator = new DeadAssignmentEliminator();
    assignmentEliminator.interceptBody(builder);

    // After eliminating dead assignments, it's posssible, there could be unused locals in body
    // If so, eliminate it/them
    BodyInterceptor localEliminator = new UnusedLocalEliminator();
    localEliminator.interceptBody(builder);
  }
}
