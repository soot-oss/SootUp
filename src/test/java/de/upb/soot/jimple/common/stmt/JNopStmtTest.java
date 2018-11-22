package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.type.IntType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JNopStmtTest {

  @Test
  public void test() {

    IStmt nop = new JNopStmt();

    Assert.assertTrue(nop.equivTo(nop));
    Assert.assertTrue(nop.equivTo(new JNopStmt()));

    Assert.assertFalse(nop
            .equivTo(new JIdentityStmt(new Local("$i0", IntType.getInstance()), new JParameterRef(IntType.getInstance(), 123))));


    Assert.assertEquals("nop",nop.toString());

  }

}
