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
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;

/**
 * @author Markus Schmidt, Linghui Luo
 */
public class JReturnVoidStmtTest {

  @Test
  public void test() {
    StmtPositionInfo nop = StmtPositionInfo.getNoStmtPositionInfo();
    Stmt rStmt = new JReturnVoidStmt(nop);

    // equivTo
    assertTrue(rStmt.equivTo(rStmt));
    assertTrue(rStmt.equivTo(new JReturnVoidStmt(nop)));
    assertFalse(rStmt.equivTo(new JNopStmt(nop)));
    assertFalse(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(3), nop)));

    // toString
    assertEquals("return", rStmt.toString());
  }
}
