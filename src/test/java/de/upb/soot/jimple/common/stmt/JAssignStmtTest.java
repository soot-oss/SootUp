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

import categories.Java8Test;
import de.upb.soot.jimple.IgnoreLocalNameComparator;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class JAssignStmtTest {

  @Test
  public void test() {

    Value numConst1 = IntConstant.getInstance(42);
    Value numConst2 = IntConstant.getInstance(33102);

    Local local = new Local("$i0", IntType.INSTANCE);
    Local field = new Local("i2", IntType.INSTANCE);

    IStmt lStmt = new JAssignStmt(local, numConst1);
    IStmt fStmt = new JAssignStmt(field, numConst1);
    IStmt deepStmt = new JAssignStmt(local, new JAddExpr(numConst1, numConst2));

    // equivTo : equals
    Assert.assertTrue(lStmt.equivTo(new JAssignStmt(local, numConst1)));
    Assert.assertTrue(lStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.INSTANCE), IntConstant.getInstance(42))));

    Assert.assertTrue(
        deepStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.INSTANCE), new JAddExpr(numConst1, numConst2))));

    // equivTo: switched operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(local, numConst2)));
    Assert.assertFalse(deepStmt.equivTo(new JAssignStmt(local, new JAddExpr(numConst2, numConst1))));

    // equivTo: different operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, numConst1)));
    Assert.assertFalse(
        lStmt.equivTo(new JAssignStmt(new Local("$i100differentname", IntType.INSTANCE), IntConstant.getInstance(42))));
    Assert
        .assertFalse(lStmt.equivTo(new JAssignStmt(new Local("$i0", LongType.INSTANCE), LongConstant.getInstance(42))));

    // equivTo: different depth
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, new JAddExpr(numConst1, numConst2))));

    // toString
    Assert.assertEquals("$i0 = 42", lStmt.toString());
    Assert.assertEquals("i2 = 42", fStmt.toString());
    Assert.assertEquals("$i0 = 42 + 33102", deepStmt.toString());

    // equivTo with comparator
    Assert.assertFalse(lStmt.equivTo(deepStmt, new IgnoreLocalNameComparator() ));

  }

}
