package singleinstruction.stmt;

import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.ref.JCaughtExceptionRef;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JIdentityStmtTest {

  @Test
  public void test() {

    IView view = new JavaView(null);
    DefaultSignatureFactory factory = new DefaultSignatureFactory();

    Local thiz = new Local("$r0", new RefType(view, factory.getTypeSignature("somepackage.dummy.MyClass")));
    IStmt thisIdStmt
        = new JIdentityStmt(thiz, new JThisRef(new RefType(view, factory.getTypeSignature("somepackage.dummy.MyClass"))));

    Local param = new Local("$i0", IntType.INSTANCE);
    IStmt paramIdStmt = new JIdentityStmt(param, new JParameterRef(IntType.INSTANCE, 123));

    Local exception = new Local("$r1", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    IStmt exceptionIdStmt = new JIdentityStmt(exception, new JCaughtExceptionRef());

    // toString
    Assert.assertEquals("$r0 := @this: somepackage.dummy.MyClass", thisIdStmt.toString());
    Assert.assertEquals("$i0 := @parameter123: int", paramIdStmt.toString());
    Assert.assertEquals("$r1 := @caughtexception", exceptionIdStmt.toString());

    // equivTo
    Assert.assertFalse(thisIdStmt
        .equivTo(new JIdentityStmt(new Local("$r5", new RefType(view, factory.getTypeSignature("somepackage.NotMyClass"))),
            new JThisRef(new RefType(view, factory.getTypeSignature("somepackage.NotMyClass"))))));
    Assert.assertFalse(thisIdStmt.equivTo(
        new JIdentityStmt(new Local("$r42", new RefType(view, factory.getTypeSignature("somepackage.dummy.MyClass"))),
            new JThisRef(new RefType(view, factory.getTypeSignature("somepackage.dummy.MyClass"))))));
    Assert.assertTrue(thisIdStmt.equivTo(thisIdStmt));
    Assert.assertFalse(thisIdStmt.equivTo(exceptionIdStmt));
    Assert.assertFalse(thisIdStmt.equivTo(paramIdStmt));

    Assert.assertFalse(
        thisIdStmt.equivTo(new JIdentityStmt(new Local("$i1", IntType.INSTANCE), new JParameterRef(IntType.INSTANCE, 123))));
    Assert.assertFalse(
        thisIdStmt.equivTo(new JIdentityStmt(new Local("$i0", IntType.INSTANCE), new JParameterRef(IntType.INSTANCE, 42))));
    Assert.assertFalse(exceptionIdStmt.equivTo(thisIdStmt));
    Assert.assertTrue(exceptionIdStmt.equivTo(exceptionIdStmt));
    Assert.assertFalse(exceptionIdStmt.equivTo(paramIdStmt));

    Assert.assertFalse(thisIdStmt
        .equivTo(new JIdentityStmt(new Local("$r1", new RefType(view, factory.getTypeSignature("somepckg.NotMyException"))),
            new JCaughtExceptionRef())));
    Assert.assertFalse(paramIdStmt.equivTo(thisIdStmt));
    Assert.assertFalse(paramIdStmt.equivTo(exceptionIdStmt));
    Assert.assertTrue(paramIdStmt.equivTo(paramIdStmt));

  }

}
