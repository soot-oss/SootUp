package de.upb.soot.jimple.common.stmt;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.constant.LongConstant;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JAssignStmtTest {

  Comparator c = new Comparator<IStmt>() {
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

    Local local = new Local("$i0", IntType.getInstance());
    Local field = new Local("i2", IntType.getInstance());

    IStmt lStmt = new JAssignStmt(local, numConst1, nop);
    IStmt fStmt = new JAssignStmt(field, numConst1, nop);
    IStmt deepStmt = new JAssignStmt(local, new JAddExpr(numConst1, numConst2), nop);
    System.out.println(deepStmt.getPositionInfo().toString());
    // equivTo : equals
    Assert.assertTrue(lStmt.equivTo(new JAssignStmt(local, numConst1, nop)));
    Assert.assertTrue(
        lStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.getInstance()), IntConstant.getInstance(42), nop)));

    Assert.assertTrue(
        deepStmt.equivTo(new JAssignStmt(new Local("$i0", IntType.getInstance()), new JAddExpr(numConst1, numConst2), nop)));

    // equivTo: switched operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(local, numConst2, nop)));
    Assert.assertFalse(deepStmt.equivTo(new JAssignStmt(local, new JAddExpr(numConst2, numConst1), nop)));

    // equivTo: different operands
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, numConst1, nop)));
    Assert.assertFalse(lStmt
        .equivTo(new JAssignStmt(new Local("$i100differentname", IntType.getInstance()), IntConstant.getInstance(42), nop)));
    Assert.assertFalse(
        lStmt.equivTo(new JAssignStmt(new Local("$i0", LongType.getInstance()), LongConstant.getInstance(42), nop)));

    // equivTo: different depth
    Assert.assertFalse(lStmt.equivTo(new JAssignStmt(field, new JAddExpr(numConst1, numConst2), nop)));

    // toString
    Assert.assertEquals("$i0 = 42", lStmt.toString());
    Assert.assertEquals("i2 = 42", fStmt.toString());
    Assert.assertEquals("$i0 = 42 + 33102", deepStmt.toString());

    // equivTo with comparator
    Assert.assertTrue(lStmt.equivTo(deepStmt, c));

  }

}
