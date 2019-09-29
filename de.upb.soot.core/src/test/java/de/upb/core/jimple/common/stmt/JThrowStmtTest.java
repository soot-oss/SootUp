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

package de.upb.core.jimple.common.stmt;

import categories.Java8Test;
import de.upb.soot.core.DefaultIdentifierFactory;
import de.upb.soot.core.jimple.basic.Local;
import de.upb.soot.core.jimple.basic.PositionInfo;
import de.upb.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.soot.core.jimple.common.stmt.Stmt;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JThrowStmtTest {

  @Test
  public void test() {
    PositionInfo nop = PositionInfo.createNoPositionInfo();
    DefaultIdentifierFactory typeFactory = DefaultIdentifierFactory.getInstance();

    Local local = new Local("$r0", typeFactory.getType("java.lang.Exception"));
    Local localEqual = new Local("$r0", typeFactory.getType("java.lang.Exception"));
    Local localDifferent = new Local("$r1", typeFactory.getType("java.lang.Exception"));
    Local localDifferent2 = new Local("$r0", typeFactory.getType("sompepackage.MyException"));

    Stmt tStmt = new JThrowStmt(local, nop);

    // equivTo
    Assert.assertTrue(tStmt.equivTo(tStmt));
    Assert.assertTrue(tStmt.equivTo(new JThrowStmt(localEqual, nop)));

    Assert.assertFalse(tStmt.equivTo(new JNopStmt(nop)));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent, nop)));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent2, nop)));

    // toString
    Assert.assertEquals("throw $r0", tStmt.toString());
  }
}
