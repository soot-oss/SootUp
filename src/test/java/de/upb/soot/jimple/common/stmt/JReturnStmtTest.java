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

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.constant.IntConstant;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JReturnStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    IStmt rStmt = new JReturnStmt(IntConstant.getInstance(42),nop);

    // equivTo
    Assert.assertTrue(rStmt.equivTo(rStmt));
    Assert.assertTrue(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(42),nop)));
    Assert.assertFalse(rStmt.equivTo(new JNopStmt(nop)));

    Assert.assertFalse(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(3),nop)));

    // toString
    Assert.assertEquals("return 42", rStmt.toString());
  }

}
