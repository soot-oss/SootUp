package singleinstruction.stmt;

import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.JStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;

import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JGotoStmtTest {

    @Test
    public void test(){

        IView view = new JavaView(null);
        DefaultSignatureFactory factory = new DefaultSignatureFactory();

        Local local1 = new Local("$r0", new RefType(view, factory.getTypeSignature("java.lang.Exception") ) );
        Local local2 = new Local("$r0", new RefType(view, factory.getTypeSignature("somepackage.dummy.Exception") ) );

        // IStmt
        IStmt targetStmt = new JThrowStmt( local1 );
        IStmt gStmt = new JGotoStmt( targetStmt );

        // IStmtBox
        IStmtBox targetStmtBox = new JStmtBox(targetStmt );
        IStmt gStmtBox = new JGotoStmt(targetStmtBox);

        // toString
        Assert.assertEquals("goto [?= throw $r0]", gStmt.toString());
        Assert.assertEquals("goto [?= throw $r0]", gStmtBox.toString());

        // equivTo
        Assert.assertTrue( gStmt.equivTo(gStmtBox) );
        Assert.assertFalse( gStmt.equivTo(targetStmt) );

        Assert.assertTrue( gStmt.equivTo( new JGotoStmt(new JThrowStmt(local1))) );
        Assert.assertFalse( gStmt.equivTo( new JGotoStmt(new JThrowStmt(local2))) );



    }


}
