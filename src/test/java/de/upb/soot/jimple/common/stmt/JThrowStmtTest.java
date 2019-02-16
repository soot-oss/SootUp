package de.upb.soot.jimple.common.stmt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;
/**
*
* @author Markus Schmidt & Linghui Luo
*
*/
@Category(Java8Test.class)
public class JThrowStmtTest {

  @Test
  public void test() {
    PositionInfo nop=PositionInfo.createNoPositionInfo();
    IView view = new JavaView(null);
    DefaultSignatureFactory factory = new DefaultSignatureFactory();

    Local local = new Local("$r0", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localEqual = new Local("$r0", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localDifferent = new Local("$r1", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localDifferent2 = new Local("$r0", new RefType(view, factory.getTypeSignature("sompepackage.MyException")));

    IStmt tStmt = new JThrowStmt(local,nop);

    // equivTo
    Assert.assertTrue(tStmt.equivTo(tStmt));
    Assert.assertTrue(tStmt.equivTo(new JThrowStmt(localEqual,nop)));

    Assert.assertFalse(tStmt.equivTo(new JNopStmt(nop)));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent,nop)));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent2,nop)));

    // toString
    Assert.assertEquals("throw $r0", tStmt.toString());
  }

}
