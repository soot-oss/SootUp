package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.common.constant.IntConstant;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JReturnVoidStmtTest {

  @Test
  public void test() {

    IStmt rStmt = new JReturnVoidStmt();

    // equivTo
    Assert.assertTrue(rStmt.equivTo(rStmt));
    Assert.assertTrue(rStmt.equivTo(new JReturnVoidStmt()));
    Assert.assertFalse(rStmt.equivTo(new JNopStmt()));
    Assert.assertFalse(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(3))));

    // toString
    Assert.assertEquals("return", rStmt.toString());
  }

}
