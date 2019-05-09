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
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.types.PrimitiveType;
import java.util.Comparator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Markus Schmidt & Linghui Luo */
@Category(Java8Test.class)
public class JAssignStmtTest {

  Comparator c =
      new Comparator<IStmt>() {
        @Override
        public int compare(IStmt o1, IStmt o2) {
          return o1.containsFieldRef() && o2.containsFieldRef() ? 1 : 0;
        }

        @Override
        public boolean equals(Object obj) {
          return false;
        }
      };

  @Test
  public void test() {

    PositionInfo nop = PositionInfo.createNoPositionInfo();
    Value numConst1 = IntConstant.getInstance(42);
    Value numConst2 = IntConstant.getInstance(33102);

    Local local = new Local("$i0", PrimitiveType.getInt());
    Local field = new Local("i2", PrimitiveType.getInt());

    IStmt lStmt = new JAssignStmt(local, numConst1, nop);
    IStmt fStmt = new JAssignStmt(field, numConst1, nop);
    IStmt deepStmt = new JAssignStmt(local, new JAddExpr(numConst1, numConst2), nop);

    // equivTo : equals
    Assert.assertTrue(lStmt.equivTo(new JAssignStmt(local, numConst1, nop)));
    Assert.assertTrue(
        lStmt.equivTo(
            new JAssignStmt(
                new Local("$i0", PrimitiveType.getInt()), IntConstant.getInstance(42), nop)));

    Assert.assertTrue(
        deepStmt.equivTo(
            new JAssignStmt(
                new Local("$i0", PrimitiveType.getInt()),
                new JAddExpr(numConst1, numConst2),
                nop)));

    // equivTo: switched operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(local, numConst2, nop)));
    Assert.assertFalse(
        deepStmt.equivTo(new JAssignStmt(local, new JAddExpr(numConst2, numConst1), nop)));

    // equivTo: different operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, numConst1, nop)));
    Assert.assertFalse(
        lStmt.equivTo(
            new JAssignStmt(
                new Local("$i100differentname", PrimitiveType.getInt()),
                IntConstant.getInstance(42),
                nop)));
    Assert.assertFalse(
        lStmt.equivTo(
            new JAssignStmt(
                new Local("$i0", PrimitiveType.getLong()), LongConstant.getInstance(42), nop)));

    // equivTo: different depth
    Assert.assertFalse(
        lStmt.equivTo(new JAssignStmt(field, new JAddExpr(numConst1, numConst2), nop)));

    // toString
    Assert.assertEquals("$i0 = 42", lStmt.toString());
    Assert.assertEquals("i2 = 42", fStmt.toString());
    Assert.assertEquals("$i0 = 42 + 33102", deepStmt.toString());

    // equivTo with comparator
    Assert.assertFalse(lStmt.equivTo(deepStmt, new IgnoreLocalNameComparator()));
  }
}
