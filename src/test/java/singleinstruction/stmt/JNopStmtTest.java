package singleinstruction.stmt;

import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.type.IntType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JNopStmtTest {

  @Test
  public void test() {

    IStmt nop = new JNopStmt(PositionInfo.createNoPositionInfo());

    Assert.assertTrue(nop.equivTo(nop));
    Assert.assertTrue(nop.equivTo(new JNopStmt(PositionInfo.createNoPositionInfo())));

    Assert.assertFalse(nop.equivTo(new JIdentityStmt(new Local("$i0", IntType.INSTANCE),
        new JParameterRef(IntType.INSTANCE, 123), PositionInfo.createNoPositionInfo())));

    Assert.assertEquals("nop", nop.toString());

  }

}
