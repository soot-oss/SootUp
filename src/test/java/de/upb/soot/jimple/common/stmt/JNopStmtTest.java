package de.upb.soot.jimple.common.stmt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.type.IntType;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JNopStmtTest {

  @Test
  public void test() {
     PositionInfo nopos=PositionInfo.createNoPositionInfo();
    IStmt nop = new JNopStmt(nopos);

    Assert.assertTrue(nop.equivTo(nop));
    Assert.assertTrue(nop.equivTo(new JNopStmt(nopos)));

    Assert.assertFalse(nop
            .equivTo(new JIdentityStmt(new Local("$i0", IntType.getInstance()), new JParameterRef(IntType.getInstance(), 123),nopos)));


    Assert.assertEquals("nop",nop.toString());

  }

}
