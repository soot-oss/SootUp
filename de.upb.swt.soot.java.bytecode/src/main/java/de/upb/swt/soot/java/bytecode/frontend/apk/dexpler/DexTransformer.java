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
import de.upb.swt.soot.core.analysis.LocalUses;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JCastExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewArrayExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DexTransformer implements BodyInterceptor {

  /**
   * Collect definitions of l in bodyBuilder including the definitions of aliases of l.
   * 
   * In this context an alias is a local that propagates its value to l.
   * 
   * @param l
   *          the local whose definitions are to collect
   * @param localDefs
   *          the LocalDefs object
   * @param bodyBuilder
   *          the bodyBuilder that contains the local
   */
  protected List<Stmt> collectDefinitionsWithAliases(Local l, LocalDefs localDefs, LocalUses localUses, Body.BodyBuilder bodyBuilder) {
    Set<Local> seenLocals = new HashSet<Local>();
    List<Local> newLocals = new ArrayList<Local>();
    List<Stmt> defs = new ArrayList<Stmt>();
    newLocals.add(l);
    seenLocals.add(l);

    while (!newLocals.isEmpty()) {
      Local local = newLocals.remove(0);
      for (Stmt stmt : collectDefinitions(local, localDefs)) {
        if (stmt instanceof JAssignStmt) {
          Value r = ((JAssignStmt) stmt).getRightOp();
          if (r instanceof Local && seenLocals.add((Local) r)) {
            newLocals.add((Local) r);
          }
        }
        defs.add(stmt);
        //
        List<Pair<Stmt, Value>> usesOf = localUses.getUsesOf(stmt);
        for (Pair<Stmt, Value> pair : usesOf) {
          Stmt stmt1 = pair.getKey();
          if (stmt1 instanceof JAssignStmt) {
            JAssignStmt assignStmt = ((JAssignStmt) stmt1);
            Value right = assignStmt.getRightOp();
            Value left = assignStmt.getLeftOp();
            if (right == local && left instanceof Local && seenLocals.add((Local) left)) {
              newLocals.add((Local) left);
            }
          }
        }
        //
      }
    }
    return defs;
  }

  /**
   * Convenience method that collects all definitions of l.
   * 
   * @param l
   *          the local whose definitions are to collect
   * @param localDefs
   *          the LocalDefs object
   */
  private List<Stmt> collectDefinitions(Local l, LocalDefs localDefs) {
    return localDefs.getDefsOf(l);
  }

  protected Type findArrayType(LocalDefs localDefs, Stmt arrayStmt, int depth, Set<Stmt> alreadyVisitedDefs) {
    JArrayRef jArrayRef = null;
    if (arrayStmt.containsArrayRef()) {
      jArrayRef = arrayStmt.getArrayRef();
    }
    Local aBase = null;

    if (null == jArrayRef) {
      if (arrayStmt instanceof JAssignStmt) {
        JAssignStmt stmt = (JAssignStmt) arrayStmt;
        aBase = (Local) stmt.getRightOp();
      } else {
        throw new RuntimeException("ERROR: not an assign statement: " + arrayStmt);
      }
    } else {
      aBase = (Local) jArrayRef.getBase();
    }

    List<Stmt> defsOfaBaseList = localDefs.getDefsOfAt(aBase, arrayStmt);
    if (defsOfaBaseList == null || defsOfaBaseList.isEmpty()) {
      throw new RuntimeException("ERROR: no def statement found for array base local " + arrayStmt);
    }

    // We should find an answer only by processing the first item of the
    // list
    Type aType = null;
    int nullDefCount = 0;
    for (Stmt baseDef : defsOfaBaseList) {
      if (alreadyVisitedDefs.contains(baseDef)) {
        continue;
      }
      Set<Stmt> newVisitedDefs = new HashSet<Stmt>(alreadyVisitedDefs);
      newVisitedDefs.add(baseDef);

      // baseDef is either an assignment statement or an identity
      // statement
      if (baseDef instanceof JAssignStmt) {
        JAssignStmt stmt = (JAssignStmt) baseDef;
        Value r = stmt.getRightOp();
        if (r instanceof JFieldRef) {
          Type t = r.getType();
          if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            t = at.getBaseType();
          }
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
        } else if (r instanceof JArrayRef) {
          JArrayRef ar = (JArrayRef) r;
          if (ar.getType().toString().equals(".unknown") || ar.getType().toString().equals("unknown")) { // ||
            // ar.getType())
            // {
            Type t = findArrayType(localDefs, stmt, ++depth, newVisitedDefs); // TODO: which type should be
            // returned?
            if (t instanceof ArrayType) {
              ArrayType at = (ArrayType) t;
              t = at.getBaseType();
            }
            if (depth == 0) {
              aType = t;
              break;
            } else {
              return t;
            }
          } else {
            ArrayType at = (ArrayType) stmt.getRightOp().getType();
            Type t = at.getBaseType();
            if (depth == 0) {
              aType = t;
              break;
            } else {
              return t;
            }
          }
        } else if (r instanceof JNewExpr) {
          JNewExpr expr = (JNewExpr) r;
          Type t = expr.getType();
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
        } else if (r instanceof JNewArrayExpr) {
          JNewArrayExpr expr = (JNewArrayExpr) r;
          Type t = expr.getBaseType();
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
        } else if (r instanceof JCastExpr) {
          Type t = (((JCastExpr) r).getType());
          if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            t = at.getBaseType();
          }
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
        } else if (r instanceof AbstractInvokeExpr) {
          Type t = ((AbstractInvokeExpr) r).getMethodSignature().getType();
          if (t instanceof ArrayType) {
            ArrayType at = (ArrayType) t;
            t = at.getBaseType();
          }
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
          // introduces alias. We look whether there is any type
          // information associated with the alias.
        } else if (r instanceof Local) {
          Type t = findArrayType(localDefs, stmt, ++depth, newVisitedDefs);
          if (depth == 0) {
            aType = t;
            // break;
          } else {
            // return t;
            aType = t;
          }
        } else if (r instanceof Constant) {
          // If the right side is a null constant, we might have a
          // case of broken code, e.g.,
          // a = null;
          // a[12] = 42;
          nullDefCount++;
        } else {
          throw new RuntimeException(String.format("ERROR: def statement not possible! Statement: %s, right side: %s",
              stmt.toString(), r.getClass().getName()));
        }

      } else if (baseDef instanceof JIdentityStmt) {
        JIdentityStmt stmt = (JIdentityStmt) baseDef;
        ArrayType at = (ArrayType) stmt.getRightOp().getType();
        Type t = at.getBaseType();
        if (depth == 0) {
          aType = t;
          break;
        } else {
          return t;
        }
      } else {
        throw new RuntimeException("ERROR: base local def must be AssignStmt or IdentityStmt! " + baseDef);
      }

      if (aType != null) {
        break;
      }
    } // loop

    if (depth == 0 && aType == null) {
      if (nullDefCount == defsOfaBaseList.size()) {
        return NullType.getInstance();
      } else {
        throw new RuntimeException("ERROR: could not find type of array from statement '" + arrayStmt + "'");
      }
    } else {
      return aType;
    }
  }

}
