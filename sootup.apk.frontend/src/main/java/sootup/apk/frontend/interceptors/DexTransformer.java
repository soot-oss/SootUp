package sootup.apk.frontend.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Markus Schmidt and others
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;

public abstract class DexTransformer implements BodyInterceptor {
  protected Type findArrayType(
      DexDefUseAnalysis dexDefUseAnalysis,
      Stmt arrayStmt,
      int depth,
      Set<Stmt> alreadyVisitedDefs) {
    JArrayRef aRef = null;
    if (arrayStmt.containsArrayRef()) {
      aRef = arrayStmt.getArrayRef();
    }
    Local aBase = null;

    if (null == aRef) {
      if (arrayStmt instanceof JAssignStmt) {
        JAssignStmt stmt = (JAssignStmt) arrayStmt;
        aBase = (Local) stmt.getRightOp();
      } else {
        throw new IllegalStateException("ERROR: not an assign statement: " + arrayStmt);
      }
    } else {
      aBase = (Local) aRef.getBase();
    }

    List<Stmt> defsOfaBaseList = dexDefUseAnalysis.getDefsOf(aBase);
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
            t = at.getElementType();
          }
          if (depth == 0) {
            aType = t;
            break;
          } else {
            return t;
          }
        } else if (r instanceof JArrayRef) {
          JArrayRef ar = (JArrayRef) r;
          if (ar.getType().toString().equals(".unknown")
              || ar.getType().toString().equals("unknown")) { // ||
            // ar.getType())
            // {
            Type t = findArrayType(dexDefUseAnalysis, stmt, ++depth, newVisitedDefs);
            if (t instanceof ArrayType) {
              ArrayType at = (ArrayType) t;
              t = at.getElementType();
            }
            if (depth == 0) {
              aType = t;
              break;
            } else {
              return t;
            }
          } else {
            ArrayType at = (ArrayType) stmt.getRightOp().getType();
            Type t = at.getElementType();
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
            t = at.getElementType();
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
            t = at.getElementType();
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
          Type t = findArrayType(dexDefUseAnalysis, stmt, ++depth, newVisitedDefs);
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
          //          throw new RuntimeException(
          //              String.format(
          //                  "ERROR: def statement not possible! Statement: %s, right side: %s",
          //                      stmt, r.getClass().getName()));
        }

      } else if (baseDef instanceof JIdentityStmt) {
        JIdentityStmt stmt = (JIdentityStmt) baseDef;
        Type t = stmt.getRightOp().getType();
        if (t instanceof ArrayType) {
          ArrayType at = (ArrayType) t;
          t = at.getElementType();
        }
        if (depth == 0) {
          aType = t;
          break;
        } else {
          return t;
        }
      } else {
        throw new IllegalStateException(
            "ERROR: base local def must be AssignStmt or IdentityStmt! " + baseDef);
      }

      if (aType != null) {
        break;
      }
    } // loop

    if (depth == 0 && aType == null) {
      if (nullDefCount == defsOfaBaseList.size()) {
        return NullType.getInstance();
      } else {
        throw new IllegalStateException(
            "ERROR: could not find type of array from statement '" + arrayStmt + "'");
      }
    } else {
      return aType;
    }
  }

  protected boolean examineInvokeExpr(AbstractInvokeExpr abstractInvokeExpr, Local l) {
    List<Immediate> args = abstractInvokeExpr.getArgs();
    List<Type> argTypes = abstractInvokeExpr.getMethodSignature().getParameterTypes();
    assert args.size() == argTypes.size();
    for (int i = 0; i < args.size(); i++) {
      if (args.get(i) == l && isFloatingPointLike(argTypes.get(i))) {
        return true;
      }
    }
    return false;
  }

  protected boolean isFloatingPointLike(Type t) {
    return (t instanceof PrimitiveType.FloatType || t instanceof PrimitiveType.DoubleType);
  }
}
