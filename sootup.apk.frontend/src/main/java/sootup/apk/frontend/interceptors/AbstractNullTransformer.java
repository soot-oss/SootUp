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

import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.LongConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.expr.JNeExpr;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.types.ReferenceType;
import sootup.core.types.Type;

/**
 * Abstract base class for {@link DexNullTransformer}
 *
 * @author Palaniappan Muthuraman
 */
public abstract class AbstractNullTransformer extends DexTransformer {
  /**
   * Examine expr if it is a comparison with 0.
   *
   * @param expr the ConditionExpr to examine
   */
  protected boolean isZeroComparison(AbstractConditionExpr expr) {
    if (expr instanceof JEqExpr || expr instanceof JNeExpr) {
      return (expr.getOp2() instanceof IntConstant && ((IntConstant) expr.getOp2()).getValue() == 0)
          || (expr.getOp2() instanceof LongConstant
              && ((LongConstant) expr.getOp2()).getValue() == 0);
    }
    return false;
  }

  /**
   * Replace 0 with null in the given unit.
   *
   * @param stmt the unit where 0 will be replaced with null.
   */
  protected void replaceWithNull(Stmt stmt) {
    if (stmt instanceof JIfStmt) {
      AbstractConditionExpr expr = ((JIfStmt) stmt).getCondition();
      if (isZeroComparison(expr)) {
        expr.withOp2(NullConstant.getInstance());
      }
    } else if (stmt instanceof JAssignStmt) {
      JAssignStmt s = (JAssignStmt) stmt;
      Value v = s.getRightOp();
      if ((v instanceof IntConstant && ((IntConstant) v).getValue() == 0)
          || (v instanceof LongConstant && ((LongConstant) v).getValue() == 0)) {
        // If this is a field assignment, double-check the type. We
        // might have a.f = 2 with a being a null candidate, but a.f
        // being an int.
        if (!(s.getLeftOp() instanceof JInstanceFieldRef)
            || s.getLeftOp().getType() instanceof ReferenceType) {
          s.withRValue(NullConstant.getInstance());
        }
      }
    }
  }

  protected static boolean isObject(Type t) {
    return t instanceof ReferenceType;
  }
}
