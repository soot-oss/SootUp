package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JStmtBox;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.NopEliminator;
import de.upb.swt.soot.java.bytecode.interceptors.UnusedLocalEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

@Category(Java8Test.class)
public class UnusedLocalEliminatorTest {

    @Test
    public void testNoInput() {
        Set<Local> locals = Collections.emptySet();
        List<Trap> traps = Collections.emptyList();
        List<Stmt> stmts = Collections.emptyList();
        Body testBody = new Body(locals, traps, stmts, null);
        Body processedBody = new NopEliminator().interceptBody(testBody);

        assertNotNull(processedBody);
        assertArrayEquals(testBody.getStmts().toArray(), processedBody.getStmts().toArray());
    }

    @Test
    public void testRemoveDefs() {

    }

    @Test
    public void testRemoveUses() {

    }

    @Test
    public void testRemoveDefsAndUses() {

    }

    @Test
    public void testRemoveNothing() {
        Body originalBody = createBody();
        Body processedBody = new UnusedLocalEliminator().interceptBody(originalBody);

        assertArrayEquals(processedBody.getStmts().toArray(), processedBody.getStmts().toArray());
    }

    private static Body createBody() {
        JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
        JavaJimple javaJimple = JavaJimple.getInstance();
        StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

        JavaClassType objectType = factory.getClassType("java.lang.Object");
        JavaClassType stringType = factory.getClassType("java.lang.String");
        Local a = JavaJimple.newLocal("a", objectType);
        Local b = JavaJimple.newLocal("b", stringType);

        Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
        Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
        Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
        Stmt jump = JavaJimple.newGotoStmt(bToA, noPositionInfo);

        Set<Local> locals = ImmutableUtils.immutableSet(a, b);
        List<Trap> traps = new ArrayList<>();
        List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);

        return new Body(locals, traps, stmts, null);
    }
}
