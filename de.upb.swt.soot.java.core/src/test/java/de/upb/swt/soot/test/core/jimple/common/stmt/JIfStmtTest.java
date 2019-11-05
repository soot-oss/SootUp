/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package de.upb.swt.soot.test.core.jimple.common.stmt;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.ConditionExprBox;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.expr.Expr;
import de.upb.swt.soot.core.jimple.common.expr.JEqExpr;
import de.upb.swt.soot.core.jimple.common.stmt.JIfStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JIfStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    Stmt target = new JNopStmt(nop);

    Expr condition = new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123));
    ConditionExprBox conditionBox = new ConditionExprBox(condition);
    Stmt ifStmt = new JIfStmt(conditionBox.getValue(), target, nop);

    // toString
    Assert.assertEquals("if 42 == 123 goto nop", ifStmt.toString());

    // equivTo
    Assert.assertFalse(ifStmt.equivTo(new JNopStmt(nop)));

    Assert.assertTrue(ifStmt.equivTo(ifStmt));
    Assert.assertTrue(ifStmt.equivTo(new JIfStmt(conditionBox.getValue(), target, nop)));
    Assert.assertTrue(
        ifStmt.equivTo(
            new JIfStmt(
                new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123)),
                target,
                nop)));

    Assert.assertFalse(
        ifStmt.equivTo(
            new JIfStmt(
                new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(666)),
                target,
                nop)));
  }
}
