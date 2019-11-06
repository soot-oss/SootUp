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
import de.upb.swt.soot.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.JLookupSwitchStmt;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JLookupSwitchStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    ArrayList<IntConstant> lookupValues = new ArrayList<>();
    ArrayList<Stmt> targets = new ArrayList<>();

    Stmt stmt =
        new JLookupSwitchStmt(
            IntConstant.getInstance(42), lookupValues, targets, new JNopStmt(nop), nop);
    Stmt stmtDifferentKey =
        new JLookupSwitchStmt(
            IntConstant.getInstance(123), lookupValues, targets, new JNopStmt(nop), nop);
    Stmt stmtDifferentDefault =
        new JLookupSwitchStmt(
            IntConstant.getInstance(42),
            lookupValues,
            targets,
            new JReturnStmt(IntConstant.getInstance(42), nop),
            nop);

    // toString
    assertEquals("lookupswitch(42) {     default: goto nop; }", stmt.toString());

    targets.add(new JReturnVoidStmt(nop));
    targets.add(new JNopStmt(nop));

    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    Stmt stmtDifferentLookupAndTarget =
        new JLookupSwitchStmt(
            IntConstant.getInstance(123), lookupValues, targets, new JNopStmt(nop), nop);
    assertEquals(
        "lookupswitch(123) {     case 42: goto return;     case 33102: goto nop;     default: goto nop; }",
        stmtDifferentLookupAndTarget.toString());

    // equivTo
    assertFalse(stmt.equivTo(this));
    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(stmtDifferentLookupAndTarget));
    assertFalse(stmt.equivTo(stmtDifferentDefault));
    assertFalse(stmt.equivTo(stmtDifferentKey));
  }
}
