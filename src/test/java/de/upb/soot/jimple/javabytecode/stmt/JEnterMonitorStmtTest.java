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

package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.IntType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 *
 * @author Markus Schmidt & Linghui Luo
 *
 */
@Category(Java8Test.class)
public class JEnterMonitorStmtTest {

  @Test
  public void test() {
    PositionInfo nop = PositionInfo.createNoPositionInfo();
    Local sandman = new Local("sandman", IntType.INSTANCE);
    Local night = new Local("night", BooleanType.INSTANCE);
    Local light = new Local("light", BooleanType.INSTANCE);

    IStmt stmt = new JEnterMonitorStmt(sandman, nop);
    IStmt nightStmt = new JEnterMonitorStmt(night, nop);
    IStmt lightStmt = new JEnterMonitorStmt(light, nop);

    // toString
    assertEquals("entermonitor sandman", stmt.toString());

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
