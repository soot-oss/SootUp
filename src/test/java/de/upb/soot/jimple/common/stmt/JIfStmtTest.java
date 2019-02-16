package de.upb.soot.jimple.common.stmt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.ConditionExprBox;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.Expr;
import de.upb.soot.jimple.common.expr.JEqExpr;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JIfStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    IStmt target = new JNopStmt(nop);

    Expr condition = new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123));
    ConditionExprBox conditionBox = new ConditionExprBox(condition);
    IStmt ifStmt = new JIfStmt(conditionBox.getValue(), target,nop);

    // toString
    Assert.assertEquals("if 42 == 123 goto nop", ifStmt.toString());

    // equivTo
    Assert.assertFalse(ifStmt.equivTo(new JNopStmt(nop)));

    Assert.assertTrue(ifStmt.equivTo(ifStmt));
    Assert.assertTrue(ifStmt.equivTo(new JIfStmt(conditionBox.getValue(), target,nop)));
    Assert.assertTrue(
        ifStmt.equivTo(new JIfStmt(new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(123)), target,nop)));

    Assert.assertFalse(
        ifStmt.equivTo(new JIfStmt(new JEqExpr(IntConstant.getInstance(42), IntConstant.getInstance(666)), target,nop)));

  }

}
