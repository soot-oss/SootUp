//
// (c) 2012 University of Luxembourg - Interdisciplinary Centre for
// Security Reliability and Trust (SnT) - All rights reserved
//
// Author: Alexandre Bartel
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2018 Raja Vallée-Rai and others
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

import de.upb.swt.soot.core.analysis.LocalDefs;
import de.upb.swt.soot.core.graph.ExceptionalStmtGraph;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.toolkits.scalar.LocalCreation;
import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * If Dalvik bytecode contains statements using a base array which is always null, Soot's fast type resolver will fail with
 * the following exception: "Exception in thread " main" java.lang.RuntimeException: Base of array reference is not an
 * array!"
 *
 * Those statements are replaced by a throw statement (this is what will happen in practice if the code is executed).
 *
 * @author alex
 * @author Steven Arzt
 *
 */
public class DexNullArrayRefTransformer implements BodyInterceptor {

  @Override
  public void interceptBody(@Nonnull Body.BodyBuilder bodyBuilder) {
    final ExceptionalStmtGraph g = new ExceptionalStmtGraph(bodyBuilder, new DalvikThrowAnalysis());
    final LocalDefs defs = G.v().soot_toolkits_scalar_LocalDefsFactory().newLocalDefs(g);
    final LocalCreation lc = new LocalCreation(bodyBuilder.getLocals(), "ex");

    boolean changed = false;
    for (Stmt stmt : bodyBuilder.getStmts()) {
      if (stmt.containsArrayRef()) {
        // Check array reference
        Value base = stmt.getArrayRef().getBase();
        if (isAlwaysNullBefore(stmt, (Local) base, defs)) {
          createThrowStmt(bodyBuilder, stmt, lc);
          changed = true;
        }
      } else if (stmt instanceof AssignStmt) {
        AssignStmt ass = (AssignStmt) stmt;
        Value rightOp = ass.getRightOp();
        if (rightOp instanceof LengthExpr) {
          // Check lengthof expression
          LengthExpr l = (LengthExpr) ass.getRightOp();
          Value base = l.getOp();
          if (base instanceof IntConstant) {
            IntConstant ic = (IntConstant) base;
            if (ic.value == 0) {
              createThrowStmt(bodyBuilder, stmt, lc);
              changed = true;
            }
          } else if (base == NullConstant.v() || isAlwaysNullBefore(stmt, (Local) base, defs)) {
            createThrowStmt(bodyBuilder, stmt, lc);
            changed = true;
          }
        }
      }
    }

    if (changed) {
      UnreachableCodeEliminator.v().transform(bodyBuilder);
    }
  }

  /**
   * Checks whether the given local is guaranteed to be always null at the given statement
   *
   * @param s
   *          The statement at which to check the local
   * @param base
   *          The local to check
   * @param defs
   *          The definition analysis object to use for the check
   * @return True if the given local is guaranteed to always be null at the given statement, otherwise false
   */
  private boolean isAlwaysNullBefore(Stmt s, Local base, LocalDefs defs) {
    List<Unit> baseDefs = defs.getDefsOfAt(base, s);
    if (baseDefs.isEmpty()) {
      return true;
    }

    for (Unit u : baseDefs) {
      if (!(u instanceof DefinitionStmt)) {
        return false;
      }
      DefinitionStmt defStmt = (DefinitionStmt) u;
      if (defStmt.getRightOp() != NullConstant.v()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a new statement that throws a NullPointerException
   *
   * @param bodyBuilder
   *          The bodyBuilder in which to create the statement
   * @param oldStmt
   *          The old faulty statement that shall be replaced with the exception
   * @param lc
   *          The object for creating new locals
   */
  private void createThrowStmt(Body.BodyBuilder bodyBuilder, Stmt oldStmt, LocalCreation lc) {
    RefType tp = RefType.v("java.lang.NullPointerException");
    Local lcEx = lc.newLocal(tp);

    MethodSignature constructorRef
        = Scene.v().makeConstructorRef(tp.getSootClass(), Collections.singletonList((Type) RefType.v("java.lang.String")));

    // Create the exception instance
    Stmt newExStmt = Jimple.newAssignStmt(lcEx, Jimple.newNewExpr(tp));
    bodyBuilder.getStmts().insertBefore(newExStmt, oldStmt);
    Stmt invConsStmt = Jimple.newInvokeStmt(Jimple.newSpecialInvokeExpr(lcEx, constructorRef,
        Collections.singletonList(StringConstant.v("Invalid array reference replaced by Soot"))));
    bodyBuilder.getUnits().insertBefore(invConsStmt, oldStmt);

    // Throw the exception
    bodyBuilder.getUnits().swapWith(oldStmt, Jimple.v().newThrowStmt(lcEx));
  }

}
