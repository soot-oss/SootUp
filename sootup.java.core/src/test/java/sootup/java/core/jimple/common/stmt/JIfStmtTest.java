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

package sootup.java.core.jimple.common.stmt;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.AbstractConditionExpr;
import sootup.core.jimple.common.expr.JEqExpr;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * @author Markus Schmidt, Linghui Luo
 */
public class JIfStmtTest {
  // TODO: [ms] incorporate Printer i.e. Body+Targets
  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Stmt target = new JNopStmt(nop);

    AbstractConditionExpr condition =
        new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123));
    Stmt ifStmt = new JIfStmt(condition, nop);

    // toString
    assertEquals("if 42 == 123", ifStmt.toString());

    // equivTo
    assertFalse(ifStmt.equivTo(new JNopStmt(nop)));

    assertTrue(ifStmt.equivTo(ifStmt));
    assertTrue(ifStmt.equivTo(new JIfStmt(condition, nop)));
    assertTrue(
        ifStmt.equivTo(
            new JIfStmt(
                new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123)), nop)));

    // switched Operands on Equal
    assertTrue(
        ifStmt.equivTo(
            new JIfStmt(
                new JEqExpr(IntConstant.getInstance(123), IntConstant.getInstance(42)), nop)));

    assertFalse(
        ifStmt.equivTo(
            new JIfStmt(
                new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(666)), nop)));
  }
}
