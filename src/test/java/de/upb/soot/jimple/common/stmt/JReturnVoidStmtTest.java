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
public class JReturnVoidStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    IStmt rStmt = new JReturnVoidStmt(nop);

    // equivTo
    Assert.assertTrue(rStmt.equivTo(rStmt));
    Assert.assertTrue(rStmt.equivTo(new JReturnVoidStmt(nop) ));
    Assert.assertFalse(rStmt.equivTo(new JNopStmt(nop)));
    Assert.assertFalse(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(3),nop)));

    // toString
    Assert.assertEquals("return", rStmt.toString());
  }

}
