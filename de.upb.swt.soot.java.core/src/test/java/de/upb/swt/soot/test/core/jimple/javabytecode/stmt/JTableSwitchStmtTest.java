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

package de.upb.swt.soot.test.core.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JTableSwitchStmt;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JTableSwitchStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<Stmt> targets = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    targets.add(new JNopStmt(nop));
    Stmt stmt =
        new JTableSwitchStmt(
            IntConstant.getInstance(123),
            1,
            4,
            targets,
            new JReturnStmt(IntConstant.getInstance(666), nop),
            nop);

    ArrayList<Stmt> targets2 = new ArrayList<>();
    targets.add(new JReturnStmt(IntConstant.getInstance(1), nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(2), nop));
    targets.add(new JNopStmt(nop));
    targets.add(new JReturnStmt(IntConstant.getInstance(3), nop));
    Stmt stmt2 =
        new JTableSwitchStmt(
            IntConstant.getInstance(123),
            1,
            4,
            targets2,
            new JReturnStmt(IntConstant.getInstance(666), nop),
            nop);
    Stmt stmt3 =
        new JTableSwitchStmt(
            IntConstant.getInstance(456),
            1,
            4,
            targets,
            new JReturnStmt(IntConstant.getInstance(666), nop),
            nop);
    Stmt stmt4 =
        new JTableSwitchStmt(
            IntConstant.getInstance(123),
            2,
            4,
            targets,
            new JReturnStmt(IntConstant.getInstance(666), nop),
            nop);
    Stmt stmt5 =
        new JTableSwitchStmt(
            IntConstant.getInstance(123),
            1,
            5,
            targets,
            new JReturnStmt(IntConstant.getInstance(666), nop),
            nop);
    Stmt stmt6 =
        new JTableSwitchStmt(IntConstant.getInstance(123), 1, 4, targets, new JNopStmt(nop), nop);

    // toString
    assertEquals(
        "tableswitch(123) {     case 1: goto return 1;     case 2: goto return 2;     case 3: goto return 3;     case 4: goto nop;     default: goto return 666; }",
        stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(new Integer(666)));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(stmt2));

    assertFalse(stmt.equivTo(stmt2));
    assertFalse(stmt.equivTo(stmt3));
    assertFalse(stmt.equivTo(stmt4));
    assertFalse(stmt.equivTo(stmt5));
    assertFalse(stmt.equivTo(stmt6));
  }
}
