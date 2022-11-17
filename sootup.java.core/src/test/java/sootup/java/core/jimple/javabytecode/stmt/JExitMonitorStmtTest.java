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

package sootup.java.core.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.types.PrimitiveType;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JExitMonitorStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.createNoStmtPositionInfo();
    Local sandman = new Local("sandman", PrimitiveType.getInt());
    Local night = new Local("night", PrimitiveType.getBoolean());
    Local light = new Local("light", PrimitiveType.getBoolean());

    Stmt stmt = new JExitMonitorStmt(sandman, nop);
    Stmt nightStmt = new JExitMonitorStmt(night, nop);
    Stmt lightStmt = new JExitMonitorStmt(light, nop);

    // toString
    assertEquals("exitmonitor sandman", stmt.toString());

    // equivTo
    assertFalse(stmt.equivTo(sandman));

    assertTrue(stmt.equivTo(stmt));
    assertFalse(stmt.equivTo(nightStmt));
    assertFalse(stmt.equivTo(lightStmt));

    assertFalse(nightStmt.equivTo(stmt));
    assertTrue(nightStmt.equivTo(nightStmt));
    assertFalse(nightStmt.equivTo(lightStmt));

    assertFalse(lightStmt.equivTo(stmt));
    assertFalse(lightStmt.equivTo(nightStmt));
    assertTrue(lightStmt.equivTo(lightStmt));
  }
}
