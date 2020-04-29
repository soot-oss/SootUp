package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Category(Java8Test.class)

public class LocalSplitterTest {

    /**
     * for(int i = 0; i < 10; i++){
     *     i = i + 1
     * }
     * transform:
     * <pre>
     * $i0 = 0
     * $z0 = $i0 < 10
     * if $z0 == 0 goto return
     * $i1 = $i0 + 1
     * $i0 = $i1
     * $i2 = $i0
     * $i3 = $i0 + 1
     * $i0 = $i3
     * goto [?= $z0 = $i0 < 10]
     *  return
     * </pre>
     *
     * to:
     * <pre>
     * $i0 = 0
     * $z0 = $i0 < 10
     * if $z0 == 0 goto return
     * $i1 = $i0 + 1
     * $i0#0 = $i1
     * $i2 = $i0#0
     * $i3 = $i0#0 + 1
     * $i0 = $i3
     * goto [?= $z0 = $i0 < 10]
     *  return
     * </pre>
     */
    @Test
    public void testLocalSet(){
        //build the test body
        JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
        JavaJimple javaJimple = JavaJimple.getInstance();
        StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

        JavaClassType intType = factory.getClassType("int");
        JavaClassType booleanType = factory.getClassType("boolean");

        Local i0 = JavaJimple.newLocal("$i0", intType);
        Local i1 = JavaJimple.newLocal("$i1", intType);
        Local i2 = JavaJimple.newLocal("$i2", intType);
        Local i3 = JavaJimple.newLocal("$i3", intType);
        Local z0 = JavaJimple.newLocal("$z0", booleanType);

        Stmt ret = JavaJimple.newReturnVoidStmt(noStmtPositionInfo);
        Stmt stmt1 = JavaJimple.newAssignStmt(i0, IntConstant.getInstance(0), noStmtPositionInfo);
        System.out.println();
        Stmt stmt2 = JavaJimple.newAssignStmt(z0, JavaJimple.newLtExpr(i0, IntConstant.getInstance(10)), noStmtPositionInfo);
        Stmt stmt3 = JavaJimple.newIfStmt(JavaJimple.newEqExpr(z0, IntConstant.getInstance(0)), ret, noStmtPositionInfo);
        Stmt stmt4 = JavaJimple.newAssignStmt(i1, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)), noStmtPositionInfo);
        Stmt stmt5 = JavaJimple.newAssignStmt(i0, i1, noStmtPositionInfo);
        Stmt stmt6 = JavaJimple.newAssignStmt(i2, i0, noStmtPositionInfo);
        Stmt stmt7 = JavaJimple.newAssignStmt(i3, JavaJimple.newAddExpr(i0, IntConstant.getInstance(1)), noStmtPositionInfo);
        Stmt stmt8 = JavaJimple.newAssignStmt(i0, i3, noStmtPositionInfo);
        Stmt stmt9 = JavaJimple.newGotoStmt(stmt2,noStmtPositionInfo);

        Set<Local> locals = ImmutableUtils.immutableSet(i0, i1, i2, i3, z0);
        List<Trap> traps = Collections.emptyList();
        List<Stmt> stmts = ImmutableUtils.immutableList(stmt1, stmt2, stmt3, stmt4, stmt5, stmt6, stmt7, stmt8, stmt9, ret);
        Body testBody = new Body(locals, traps, stmts, null);

        LocalSplitter localSplitter = new LocalSplitter();
        Body newBody = localSplitter.interceptBody(testBody);

        Local expectedNewLocal = JavaJimple.newLocal("$i0#0", intType);
        Set<Local> expectedLocals = new HashSet<>(testBody.getLocals());
        expectedLocals.add(expectedNewLocal);
        assertEquals(newBody.getLocals(), expectedLocals);
    }
}
