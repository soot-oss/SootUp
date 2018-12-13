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

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.basic.ConditionExprBox;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.Expr;
import de.upb.soot.jimple.common.expr.JEqExpr;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JIfStmtTest {

  @Test
  public void test() {

    IStmt target = new JNopStmt();

    Expr condition = new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123));
    ConditionExprBox conditionBox = new ConditionExprBox(condition);
    IStmt ifStmt = new JIfStmt(conditionBox.getValue(), target);

    // toString
    Assert.assertEquals("if 42 == 123 goto nop", ifStmt.toString());

    // equivTo
    Assert.assertFalse(ifStmt.equivTo(new JNopStmt()));

    Assert.assertTrue(ifStmt.equivTo(ifStmt));
    Assert.assertTrue(ifStmt.equivTo(new JIfStmt(conditionBox.getValue(), target)));
    Assert.assertTrue(
        ifStmt.equivTo(new JIfStmt(new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123)), target)));

    Assert.assertFalse(
        ifStmt.equivTo(new JIfStmt(new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(666)), target)));

  }

}
