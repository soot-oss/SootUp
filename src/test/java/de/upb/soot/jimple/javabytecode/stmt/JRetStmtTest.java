package de.upb.soot.jimple.javabytecode.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.soot.jimple.basic.ImmediateBox;
import de.upb.soot.jimple.basic.VariableBox;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.IStmt;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JRetStmtTest {

    @Test
    public void test() {

        IStmt stmt = new JRetStmt( new ImmediateBox(IntConstant.getInstance(33102)  ));
        IStmt stmt2 = new JRetStmt( new ImmediateBox(IntConstant.getInstance(42)  ));

        // toString
        assertEquals("ret 33102", stmt.toString());

        // equivTo
        assertFalse(stmt.equivTo( this ));
        assertTrue(stmt.equivTo( stmt));
        assertFalse(stmt.equivTo( stmt2 ));

    }

}