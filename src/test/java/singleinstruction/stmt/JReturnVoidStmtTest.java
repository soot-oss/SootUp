package singleinstruction.stmt;

import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;

import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
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
    Assert.assertTrue(rStmt.equivTo(new JReturnVoidStmt() ));
    Assert.assertFalse(rStmt.equivTo(new JNopStmt()));
    Assert.assertFalse(rStmt.equivTo(new JReturnStmt(IntConstant.getInstance(3))));

    // toString
    Assert.assertEquals("return", rStmt.toString());
  }

}
