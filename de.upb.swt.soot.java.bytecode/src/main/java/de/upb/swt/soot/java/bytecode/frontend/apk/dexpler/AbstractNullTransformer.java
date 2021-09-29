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


import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractConditionExpr;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.expr.JEqExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JInstanceFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;

/**
 * Abstract base class for {@link DexNullTransformer} and {@link DexIfTransformer}.
 *
 * @author Steven Arzt
 */
public abstract class AbstractNullTransformer extends DexTransformer {

    /**
     * Examine expr if it is a comparison with 0.
     *
     * @param expr
     *          the ConditionExpr to examine
     */
    protected boolean isZeroComparison(Expr expr) {
        if (expr instanceof JEqExpr || expr instanceof JNeExpr) {
            if (((AbstractConditionExpr) expr).getOp2() instanceof IntConstant && ((IntConstant) ((AbstractConditionExpr) expr).getOp2()).getValue() == 0) {
                return true;
            }
            if (((AbstractConditionExpr) expr).getOp2() instanceof LongConstant && ((LongConstant) ((AbstractConditionExpr) expr).getOp2()).getValue() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replace 0 with null in the given unit.
     *
     * @param u
     *          the unit where 0 will be replaced with null.
     */
    protected void replaceWithNull(Stmt u) {
        if (u instanceof JIfStmt) {
            Expr expr = (Expr) ((JIfStmt) u).getCondition();
            if (isZeroComparison(expr)) {
                expr.setOp2(NullConstant.getInstance());
            }
        } else if (u instanceof JAssignStmt) {
            JAssignStmt s = (JAssignStmt) u;
            Value v = s.getRightOp();
            if ((v instanceof IntConstant && ((IntConstant) v).getValue() == 0)
                    || (v instanceof LongConstant && ((LongConstant) v).getValue() == 0)) {
                // If this is a field assignment, double-check the type. We
                // might have a.f = 2 with a being a null candidate, but a.f
                // being an int.
                if (!(s.getLeftOp() instanceof JInstanceFieldRef)
                        || ((JInstanceFieldRef) s.getLeftOp()).getFieldRef().type() instanceof JInstanceFieldRef) {
                    s.setRightOp(NullConstant.getInstance());
                }
            }
        }
    }

    protected static boolean isObject(Type t) {
        return t instanceof ReferenceType;
    }

}